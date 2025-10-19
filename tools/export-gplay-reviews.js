#!/usr/bin/env node
import gplay from "google-play-scraper";
import fs from "node:fs";
import path from "node:path";
import yargs from "yargs";
import { hideBin } from "yargs/helpers";

const argv = yargs(hideBin(process.argv))
  .usage("$0 <appId> [options]")
  .positional("appId", { describe: "Android package name", type: "string" })
  .option("out", { alias: "o", describe: "Output CSV path", type: "string", default: "reviews.csv" })
  .option("max", { alias: "n", describe: "Max reviews to fetch", type: "number", default: 3000 })
  .option("sort", {
    describe: "Sort: NEWEST | RATING | HELPFULNESS",
    type: "string",
    default: "NEWEST",
  })
  .option("lang", { describe: "Review language (ISO-639-1)", type: "string", default: "en" })
  .option("country", { describe: "Store country (ISO-3166-1 alpha-2)", type: "string", default: "us" })
  .strict()
  .demandCommand(1)
  .help().argv;

const csvHeader = [
  "reviewId","userName","userImage","score","title","text",
  "thumbsUp","replyText","version","appVersion","date","url"
];

const esc = (v) => {
  if (v === null || v === undefined) return "";
  const s = String(v).replace(/"/g, '""');
  return `"${s}"`;
};
const toRow = (r) =>
  [
    r.id,
    r.userName,
    r.userImage,
    r.score,
    r.title,
    r.text,
    r.thumbsUp || 0,
    r.replyText || "",
    r.version || "",
    r.appVersion || "",
    r.date ? new Date(r.date).toISOString() : "",
    r.url || ""
  ].map(esc).join(",");

async function fetchAll({ appId, max, sort, lang, country }) {
  let results = [];
  let token = undefined;

  while (results.length < max) {
    const page = await gplay.reviews({
      appId,
      lang,
      country,
      sort: gplay.sort[sort.toUpperCase()] ?? gplay.sort.NEWEST,
      paginate: true,
      nextPaginationToken: token,
    });

    results.push(...page.data);
    token = page.nextPaginationToken;
    if (!token) break;
  }
  return results.slice(0, max);
}

(async () => {
  const appId = argv._[0];
  const outPath = path.resolve(String(argv.out));

  const reviews = await fetchAll({
    appId,
    max: argv.max,
    sort: argv.sort,
    lang: argv.lang,
    country: argv.country,
  });

  const lines = [csvHeader.join(","), ...reviews.map(toRow)].join("\n");
  fs.writeFileSync(outPath, lines, "utf8");
  console.log(`Wrote ${reviews.length} reviews to ${outPath}`);
})().catch((e) => {
  console.error("Error:", e?.message || e);
  process.exit(1);
});

