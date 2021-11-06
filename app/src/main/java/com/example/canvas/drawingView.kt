package com.example.canvas

import android.content.Context
import android.graphics.*
import android.media.session.PlaybackState
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.view.*

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mDrawpath: CustomPath? = null
    private var mCanvaBitmap: Bitmap? = null
    private var mDrawpaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val mPath = ArrayList<CustomPath>()
    private val mUndo = ArrayList<CustomPath>()
    private val mRedo = ArrayList<CustomPath>()

    init {
        setupDrawing()
    }

     fun onUndo(view: View){
        if(mPath.size > 0){
            mUndo.add(mPath.removeAt(mPath.size - 1))
            invalidate()
        }
        else{
            Snackbar.make(view,"Nothing to undo",Snackbar.LENGTH_SHORT).show()
        }
    }
    fun onRedo(view: View){
        if(mUndo.size > 0){
            mPath.add((mUndo.removeAt(mUndo.size - 1)))
            invalidate()
        }
        else{
            Snackbar.make(view,"Nothing to redo",Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupDrawing() {
        mDrawpaint = Paint()
        mDrawpath = CustomPath(color, mBrushSize)
        mDrawpaint!!.color = color
        mDrawpaint!!.style = Paint.Style.STROKE
        mDrawpaint!!.strokeJoin = Paint.Join.ROUND
        mDrawpaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        // mBrushSize = 20.0.toFloat()

    }

    internal inner class CustomPath(
        var color: Int,
        var brushthickness: Float,
    ) : Path() {

    }
    fun setcolor(newColor: String){
        color = Color.parseColor(newColor)
        mDrawpaint!!.color = color
    }

    fun setNewSizeBrush(new: Float) {
        mBrushSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, new, resources.displayMetrics)
        mDrawpaint!!.strokeWidth = mBrushSize
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mCanvaBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvaBitmap!!)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawBitmap(mCanvaBitmap!!, 0f, 0f, mCanvasPaint)


        for (path in mPath) {
            mDrawpaint!!.strokeWidth = path.brushthickness
            mDrawpaint!!.color = path.color
            canvas.drawPath(path, mDrawpaint!!)
        }

        if (!mDrawpath!!.isEmpty) {
            mDrawpaint!!.strokeWidth = mDrawpath!!.brushthickness
            mDrawpaint!!.color = mDrawpath!!.color
            canvas.drawPath(mDrawpath!!, mDrawpaint!!)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchx = event!!.x
        val touchy = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawpath!!.color = color
                mDrawpath!!.brushthickness = mBrushSize
                mDrawpath!!.reset()
                mDrawpath!!.moveTo(touchx, touchy)
            }
            MotionEvent.ACTION_MOVE -> {
                mDrawpath!!.lineTo(touchx, touchy)
            }
            MotionEvent.ACTION_UP -> {
                mPath.add(mDrawpath!!)
                mDrawpath = CustomPath(color, mBrushSize)
            }


            else -> {
            }
        }
        invalidate()

        return true
    }


}


