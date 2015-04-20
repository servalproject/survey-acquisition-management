package com.delorme.inreachapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * A simple scrolling text view that will automatically
 * scroll to the bottom if text is appended.
 * 
 * @author Eric Semle
 * @since inReach SDK (11 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public class AutoScrollTextView extends ScrollView implements OnTouchListener,
    Runnable
{
    /**
     * Constructor
     * 
     * @param[in] context The application context
     * 
     * @author Eric Semle
     * @since inReach SDK (11 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public AutoScrollTextView(Context context)
    {
        super(context);
        
        initUi();
    }
    
    /**
     * Constructor
     * 
     * @param[in] context The application context
     * @param[in] attrs The attribute set
     * 
     * @author Eric Semle
     * @since inReach SDK (11 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public AutoScrollTextView(Context context, AttributeSet attrs) 
    {
        super(context, attrs);

        initUi();
    }

    /**
     * Constructor
     * 
     * @param[in] context The application context
     * @param[in] attrs The attribute set
     * @param[in] defStyle The style
     * 
     * @author Eric Semle
     * @since inReach SDK (11 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public AutoScrollTextView(Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
        
        initUi();
    }
    
    /**
     * Initializes the scroll view contents with a text view
     * 
     * @author Eric Semle
     * @since inReach SDK (11 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    protected void initUi()
    {
        m_textView = new TextView(getContext());
        m_textView.setSingleLine(false);
        m_textView.setBackgroundColor(Color.TRANSPARENT);
        m_textView.setMovementMethod(
                ScrollingMovementMethod.getInstance());
        m_textView.setLayoutParams(
                new LayoutParams(
                        LayoutParams.FILL_PARENT, 
                        LayoutParams.WRAP_CONTENT));
        m_textView.setOnTouchListener(this);
        
        addView(m_textView);
    }
    
    /**
     * Returns the text view of the AutoScrollTextView
     * 
     * @author Eric Semle
     * @since inReach SDK (11 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public TextView getTextView()
    {
        return m_textView;
    }
    
    /**
     * Appends text to the end of the text view. If the
     * view scrolled to the bottom, it will automatically
     * scroll to keep the new text within the view.
     * 
     * @param[in] text The text that is appended
     * 
     * @author Eric Semle
     * @since inReach SDK (11 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void appendText(CharSequence text)
    {
        if (text == null || m_textView == null)
        {
            return;
        }
        
        m_textView.append(text);
        
        if (!m_autoScroll)
        {
            return;
        }
        
        post(this);
    }
    
    /**
     * Called when the text view is touched
     * 
     * param[in] v The view that was touched
     * param[in] event The motion event
     * 
     * @author Eric Semle
     * @since inReach SDK (11 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            m_autoScroll = false;
        }
        
        return true;
    }
    
    /**
     * Called when the scroll has changed
     * 
     * param[in] l  Current horizontal scroll origin.
     * param[in] t  Current vertical scroll origin.
     * param[in] oldl  Previous horizontal scroll origin.
     * param[in] oldt  Previous vertical scroll origin. 

     * @author Eric Semle
     * @since inReach SDK (11 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt)
    {
        final int verticalRange = computeVerticalScrollRange();
        final int verticalHeight = computeVerticalScrollExtent();
        final int verticalOffset = getScrollY();
        final int padding = getPaddingBottom();
        final int diff = verticalRange - (verticalHeight + verticalOffset - padding);

        if (diff == 0)
        {
            m_autoScroll = true;
        }
        
        super.onScrollChanged(l, t, oldl, oldt);
    }
    
    /**
     * If auto scroll is on, this method scrolls the view
     * down
     * 
     * @author Eric Semle
     * @since inReach SDK (11 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void run()
    {
        if (m_autoScroll)
        {
            fullScroll(View.FOCUS_DOWN);
        }
    }
    
    //! The text view inside of the scroll view
    private TextView m_textView = null;
    
    //! Whether or not the view should be auto scrolled
    private boolean m_autoScroll = true;

}