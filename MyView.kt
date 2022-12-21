package com.example.colortiles

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import java.util.*

class MyView(context: Context?): View(context) {
    private val p = Paint();
    private var ar = Array(4) {Array(4){Rect(0, 0, 0, 0)} }
    private var toggle = Array(4) {Array(4){false} }
    private var sol = Array(4) {Array(4){false} }

    private var btn_hint = Rect(0, 0, 0, 0)
    private var hints = Array(4){Array(4){false} }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val layoutWidth: Int = right - left
        val layoutHeight: Int = bottom - top
        val bound = 15
        val twidth: Int = (layoutWidth - bound * 5) / 4

        for (i in ar.indices) {
            for (j in ar[i].indices) {
                ar[i][j].left = bound + j * (twidth + bound)
                ar[i][j].top = bound + i * (twidth + bound)
                ar[i][j].right = (j + 1) * (twidth + bound)
                ar[i][j].bottom = (i + 1) * (twidth + bound)
            }
        }
        btn_hint.left = twidth + bound * 2
        btn_hint.top = layoutHeight - twidth - bound
        btn_hint.right = (twidth + bound) * 3
        btn_hint.bottom = layoutHeight - bound

        randomize(50)
        solve(true)
    }

    override fun onDraw(canvas: Canvas?) {
        for (i in ar.indices) {
            for (j in ar[i].indices) {
                if (toggle[i][j]) {
                    if (hints[i][j]) {
                        p.color = Color.rgb(255, 128, 0)
                        hints[i][j] = false
                    } else {
                        p.color = Color.rgb(255, 0, 0)
                    }
                } else {
                    if (hints[i][j]) {
                        p.color = Color.rgb(0, 128, 255)
                        hints[i][j] = false
                    } else {
                        p.color = Color.rgb(0, 0, 255)
                    }
                }
                canvas?.apply {
                    drawRect(ar[i][j].left.toFloat(), ar[i][j].top.toFloat(), ar[i][j].right.toFloat(), ar[i][j].bottom.toFloat(), p)
                }
            }
        }
        p.color = Color.GREEN
        canvas?.apply {
            drawRect(btn_hint.left.toFloat(), btn_hint.top.toFloat(), btn_hint.right.toFloat(), btn_hint.bottom.toFloat(), p)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.apply {
            val rect = getRectFrom(x.toInt(), y.toInt())
            if (rect > -1) {
                toggleRect(rect / 4, rect % 4)
                solve(false,rect / 4, rect % 4)
            } else if (getRectHintButton(x.toInt(), y.toInt())) {
                val cnt: Int = countSolutionMoves()
                if (cnt > 0) {
                    val extra: Int = extractMove()
                    hints[extra / 4][extra % 4] = true
                    context.toast("Best move: " + (extra / 4).toString() + " " + (extra % 4).toString() + ", " + cnt.toString() + " left")
                } else {
                    context.toast("There are no moves left!")
                }
            }
        }
        invalidate()
        return super.onTouchEvent(event)
    }

    private fun toggleRect(x: Int, y: Int) {
        for (i in toggle.indices) {
            toggle[i][y] = !toggle[i][y]
        }
        for (j in toggle.indices) {
            toggle[x][j] = !toggle[x][j]
        }
        toggle[x][y] = !toggle[x][y]
    }

    private fun getRectFrom(x: Int, y: Int): Int {
        for (i in ar.indices) {
            for (j in ar[i].indices) {
                if (x >= ar[i][j].left && x <= ar[i][j].right && y >= ar[i][j].top && y <= ar[i][j].bottom) {
                    return j + i * 4
                }
            }
        }
        return -1
    }

    private fun getRectHintButton(x: Int, y: Int): Boolean {
        if (x >= btn_hint.left && x <= btn_hint.right && y >= btn_hint.top && y <= btn_hint.bottom) {
            return true
        }
        return false
    }

    private fun randomize(moves: Int) {
        for (i in 1..moves) {
            val x: Int = Random().nextInt(ar.size)
            val y: Int = Random().nextInt(ar.size)
            toggleRect(x, y)
        }
    }

    private fun solve(initial: Boolean, x: Int = 0, y: Int = 0) {
        if (initial) {
            for (i in toggle.indices) {
                for (j in toggle[i].indices) {
                    var cnt = 0
                    for (k in toggle.indices) {
                        if (toggle[k][j]) {
                            cnt++
                        }
                    }
                    for (l in toggle.indices) {
                        if (toggle[i][l]) {
                            cnt++
                        }
                    }
                    if (toggle[i][j]) {
                        cnt--
                    }
                    sol[i][j] = (cnt % 2).toBoolean()
                }
            }
        } else {
            sol[x][y] = !sol[x][y]
        }
        if (countSolutionMoves() == 0) {
            context.toast("The fridge is now unlocked!")
        }
    }

    private fun countSolutionMoves(): Int {
        var cnt = 0
        for (i in ar.indices) {
            for (j in ar.indices) {
                if (sol[i][j]) {
                    cnt++
                }
            }
        }
        return cnt
    }

    private fun extractMove(): Int {
        for (i in sol.indices) {
            for (j in sol[i].indices) {
                if (sol[i][j]) {
                    return j + i * 4
                }
            }
        }
        return -1
    }

    private fun Context.toast(message: CharSequence) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun Int.toBoolean(): Boolean {
        if (this == 0) {
            return false
        }
        return true
    }
}