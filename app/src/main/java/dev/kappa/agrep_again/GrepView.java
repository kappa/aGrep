package dev.kappa.agrep_again;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class GrepView extends ListView {

    static class Data implements Comparator<Data> {

        public Uri mUri;
        public String mDisplayName;
        public int mLinenumber;
        public CharSequence mText;

        public Data(){
            this(null, null, 0, null);
        }

        public Data(Uri uri, String displayName, int linenumber, CharSequence text){
            mUri = uri;
            mDisplayName = displayName;
            mLinenumber = linenumber;
            mText = text;
        }

        @Override
        public int compare(Data object1, Data object2) {
            String name1 = object1.mDisplayName != null ? object1.mDisplayName : "";
            String name2 = object2.mDisplayName != null ? object2.mDisplayName : "";
            int ret = name1.compareToIgnoreCase(name2);
            if (ret == 0) {
                ret = object1.mLinenumber - object2.mLinenumber;
            }
            return ret;
        }

    }

    interface Callback {
        void onGrepItemClicked(int position);
        boolean onGrepItemLongClicked(int position);
    }

    private Callback mCallback;

    private void init(Context context)
    {
        setSmoothScrollbarEnabled(true);
        setScrollingCacheEnabled(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setFastScrollEnabled(true);
        setBackgroundColor(ContextCompat.getColor(context, R.color.color_background));
        setCacheColorHint(ContextCompat.getColor(context, R.color.color_background));
        setDividerHeight(2);
        setOnItemClickListener((parent, view, position, id) -> {
            if (mCallback != null) {
                mCallback.onGrepItemClicked(position);
            }
        });
        setOnItemLongClickListener((parent, view, position, id) -> {
            if (mCallback != null) {
                return mCallback.onGrepItemLongClicked(position);
            }
            return false;
        });

    }

    public GrepView(Context context) {
        super(context);
        init(context);
    }

    public GrepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public GrepView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setCallback( Callback cb )
    {
        mCallback = cb;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        requestFocus();
        return super.onTouchEvent(ev);
    }


    static class GrepAdapter extends ArrayAdapter<Data>
    {

        private Pattern mPattern;
        private int mFgColor;
        private int mBgColor;
        private int mFontSize;


        static  class ViewHolder {
            TextView Index;
            TextView kwic;
        }


        public GrepAdapter(Context context, int resource, int textViewResourceId, ArrayList<Data> objects)
        {
            super(context, resource, textViewResourceId, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            final View view;
            ViewHolder holder;
            if ( convertView != null ) {
                view = convertView;
                holder = (ViewHolder) view.getTag();

            } else {
                view = inflate(getContext() , R.layout.list_row , null );

                holder = new ViewHolder();
                holder.Index = view.findViewById(R.id.ListIndex);
                holder.kwic = view.findViewById(R.id.ListPhone);

                holder.Index.setTextColor(ContextCompat.getColor(getContext(), R.color.color_on_background));
                holder.kwic.setTextColor(ContextCompat.getColor(getContext(), R.color.color_on_background));

                holder.Index.setTextSize(mFontSize);
                holder.kwic.setTextSize(mFontSize);

                view.setTag(holder);
            }
            Data d = getItem(position);

            String baseName = d.mDisplayName != null ? d.mDisplayName : "";
            String fname = baseName + "(" + d.mLinenumber + ")";
            holder.Index.setText(fname);
            holder.kwic.setText( Search.highlightKeyword(d.mText, mPattern, mFgColor , mBgColor ) );

            return view;
        }

        public void setFormat(Pattern pattern, int fgcolor, int bgcolor, int size) {
            mPattern = pattern;
            mFgColor = fgcolor;
            mBgColor = bgcolor;
            mFontSize = size;

        }
    }
}
