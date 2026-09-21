/*
 * 实验作业：多语言版交互式 Hello World（纯代码实现 Pure Code）
 * 姓名/学号：乔于恬 / 2024050133
 *
 * 说明：
 * 1. 界面完全由 Jetpack Compose 代码构建，不使用任何 XML 布局文件；
 * 2. 支持 中文 / English / 日本語 三种语言，点击语言按钮即时切换
 *    （通过 Locale + recreate 实现，选择持久化保存）；
 * 3. 随语言切换显示对应国旗图片（中文-中国 / English-英国 / 日本語-日本）；
 * 4. 输入姓名点击"向我问好"，以当前语言输出个性化问候，实现交互。
 */
package com.example.affirmations

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.affirmations.ui.theme.AffirmationsTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启动时恢复上次选择的语言（默认中文）
        applySavedLocale()
        // 纯代码构建整个界面（Compose，无任何布局 XML）
        setContent {
            AffirmationsTheme {
                HelloWorldScreen(onSwitchLanguage = ::switchLanguage)
            }
        }
    }

    /** 切换语言：保存选择 -> 应用 Locale -> 重建界面 */
    private fun switchLanguage(lang: String) {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit().putString("lang", lang).apply()
        applyLocale(lang)
        recreate()
    }

    /** 读取持久化语言并应用 */
    private fun applySavedLocale() {
        val saved = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("lang", "zh") ?: "zh"
        applyLocale(saved)
    }

    /** 应用 Locale 到资源（课程中多语言切换的标准做法） */
    @Suppress("DEPRECATION")
    private fun applyLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

/** 交互式 Hello World 主界面（纯代码） */
@Composable
fun HelloWorldScreen(onSwitchLanguage: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val lang = prefs.getString("lang", "zh") ?: "zh"

    // 当前语言对应的国旗资源
    val flagRes = when (lang) {
        "en" -> R.drawable.flag_gb
        "ja" -> R.drawable.flag_jp
        else -> R.drawable.flag_cn
    }

    var nameInput by remember { mutableStateOf("") }
    var showGreeting by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF2F5FB)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. 应用标题：姓名 + 学号
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1),
                textAlign = TextAlign.Center
            )
            // 2. 当前语言的国旗图片
            Image(
                painter = painterResource(flagRes),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 18.dp)
                    .size(100.dp)
            )
            // 3. 核心文案："你好，世界！"（随语言切换）
            Text(
                text = stringResource(R.string.hello_world),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202936),
                modifier = Modifier.padding(vertical = 14.dp),
                textAlign = TextAlign.Center
            )
            // 4. 三种语言切换按钮
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    "zh" to R.string.lang_zh,
                    "en" to R.string.lang_en,
                    "ja" to R.string.lang_ja
                ).forEach { (code, labelRes) ->
                    Button(onClick = { onSwitchLanguage(code) }) {
                        Text(stringResource(labelRes))
                    }
                }
            }
            // 5. 姓名输入框（交互输入）
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text(stringResource(R.string.greet_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            )
            // 6. 问候按钮（交互触发）
            Button(
                onClick = { showGreeting = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text(stringResource(R.string.greet_btn), fontSize = 16.sp)
            }
            // 7. 问候结果：用当前语言输出"你好，XX！"
            if (showGreeting) {
                val name = nameInput.trim().ifEmpty { context.getString(R.string.anonymous) }
                Text(
                    text = context.getString(R.string.greeting, name),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1),
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
