package jp.sblo.pandora.aGrep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.mozilla.universalchardet.UniversalDetector;

public class UniversalDetectorRegressionTest {

    private static String detect(UniversalDetector detector, byte[] data) {
        detector.handleData(data, 0, data.length);
        detector.dataEnd();
        String detected = detector.getDetectedCharset();
        detector.reset();
        return detected;
    }

    @Test
    public void detectsUtf8WithMultibyteCharacters() {
        UniversalDetector detector = new UniversalDetector(null);
        String detected = detect(detector, "こんにちは世界".getBytes(StandardCharsets.UTF_8));
        assertEquals("UTF-8", detected);
    }

    @Test
    public void detectsShiftJis() {
        UniversalDetector detector = new UniversalDetector(null);
        Charset shiftJis = Charset.forName("Shift_JIS");
        String detected = detect(detector, "あいうえお".getBytes(shiftJis));
        assertEquals("SHIFT_JIS", detected);
    }

    @Test
    public void detectsWindows1251RussianText() {
        UniversalDetector detector = new UniversalDetector(null);
        Charset windows1251 = Charset.forName("windows-1251");
        String detected = detect(detector, "Привет мир".getBytes(windows1251));
        assertEquals("WINDOWS-1251", detected);
    }

    @Test
    public void returnsNullForAsciiWhenDetectionFails() {
        UniversalDetector detector = new UniversalDetector(null);
        String detected = detect(detector, "plain ascii text".getBytes(StandardCharsets.US_ASCII));
        assertTrue(detected == null
                || "ASCII".equalsIgnoreCase(detected)
                || "US-ASCII".equalsIgnoreCase(detected));
    }

    @Test
    public void detectorCanBeReusedAfterReset() {
        UniversalDetector detector = new UniversalDetector(null);

        String utf8 = detect(detector, "Résumé".getBytes(StandardCharsets.UTF_8));
        assertEquals("UTF-8", utf8);

        Charset eucKr = Charset.forName("EUC-KR");
        String euc = detect(detector, "텍스트".getBytes(eucKr));
        assertEquals("EUC-KR", euc);
    }
}
