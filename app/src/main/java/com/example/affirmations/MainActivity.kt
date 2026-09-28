/*
 * 实验二：UI 控件代码生成 + Android MVC
 * 姓名/学号：乔于恬 / 2024050133
 *
 * MVC 分工：
 *  - Model  : ProgramModel.kt  —— 专业咨询数据仓库，提供 query() 查询；
 *  - View   : 本文件 buildUI() 中纯代码 new 出的所有控件（无任何 XML 布局），
 *             区1 使用 ScrollView 嵌套 LinearLayout 承载动态 addView 的 TextView；
 *  - Controller : MainActivity —— 接收按钮点击，调用 Model，再把结果刷新到 View。
 *
 * 规范：界面上不出现任何硬编码字符串，全部通过 R.string.* 资源间接引用。
 */
package com.example.affirmations

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    // ===== Model 层引用（Controller 持有 Model）=====
    private lateinit var programModel: ProgramModel

    // ===== View 层中需要被事件回调访问的控件 =====
    private lateinit var recordContainer: LinearLayout   // 区1：ScrollView 内的 LinearLayout
    private lateinit var resultView: TextView            // 区2：查询结果显示
    private lateinit var inputView: EditText             // 区2：专业名称输入

    private var recordCount = 0                          // 区1：已动态添加的条数

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) 初始化 Model
        programModel = ProgramModel()

        // 2) 完全用代码构建 View 并设置为内容视图
        setContentView(buildUI())
    }

    /** 纯代码构建整个界面：根 LinearLayout(vertical)，内含区1 与 区2 两个功能区 */
    private fun buildUI(): View {
        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        // 根布局
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF2F5FB.toInt())
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        // 顶部应用标题（姓名 + 学号，来自字符串资源）
        root.addView(
            TextView(this).apply {
                text = getString(R.string.app_name)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                setTextColor(0xFF0D47A1.toInt())
                setTypeface(typeface, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(16))
            }
        )

        // ==================== 区1 ====================
        root.addView(makeZoneTitle(getString(R.string.zone1_title)))

        // “添加一条记录”按钮：点击后用代码 new TextView 并 addView 进 LinearLayout
        root.addView(
            Button(this).apply {
                text = getString(R.string.btn_add)
                setOnClickListener { onAddRecord() }
            }
        )

        // ScrollView 嵌套 LinearLayout：动态添加的 TextView 放在内层 LinearLayout 中
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            ).apply { topMargin = dp(8) }
            setBackgroundColor(Color.WHITE)
            // 给滚动区一个细边框感
            elevation = dp(2).toFloat()
        }
        recordContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        scrollView.addView(recordContainer)
        root.addView(scrollView)

        // 分隔线
        root.addView(
            View(this).apply {
                setBackgroundColor(0xFFD0D7E2.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(1)
                ).apply { topMargin = dp(16); bottomMargin = dp(16) }
            }
        )

        // ==================== 区2 ====================
        root.addView(makeZoneTitle(getString(R.string.zone2_title)))

        // 专业名称输入框
        inputView = EditText(this).apply {
            hint = getString(R.string.adviser_hint)
            setSingleLine(true)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.WHITE)
        }
        root.addView(inputView)

        // “查询专业建议”按钮：Controller 事件响应 -> 调用 Model -> 刷新 View
        root.addView(
            Button(this).apply {
                text = getString(R.string.btn_query)
                setOnClickListener { onQueryAdvice() }
            }
        )

        // 查询结果显示
        resultView = TextView(this).apply {
            text = getString(R.string.result_hint)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(0xFF202936.toInt())
            setLineSpacing(0f, 1.2f)
            setPadding(dp(4), dp(12), dp(4), 0)
        }
        root.addView(resultView)

        return root
    }

    /** 生成每个功能区的小标题 TextView */
    private fun makeZoneTitle(text: String): TextView {
        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()
        return TextView(this).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setTextColor(0xFF0D47A1.toInt())
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(8))
        }
    }

    // ==================== Controller：事件处理 ====================

    /** 区1：用代码动态 new 一个 TextView，加入 ScrollView 内的 LinearLayout */
    private fun onAddRecord() {
        recordCount++
        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        val tv = TextView(this).apply {
            // 文本来自字符串资源，带序号参数
            text = getString(R.string.record_item, recordCount)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(0xFF202936.toInt())
            setPadding(dp(12), dp(14), dp(12), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        recordContainer.addView(tv)
    }

    /** 区2：取输入 -> 调用 Model 查询 -> 把结果显示到结果 TextView */
    private fun onQueryAdvice() {
        val keyword = inputView.text.toString()
        // Controller 把请求转发给 Model，再把 Model 返回的数据交给 View
        resultView.text = programModel.query(this, keyword)
    }
}
