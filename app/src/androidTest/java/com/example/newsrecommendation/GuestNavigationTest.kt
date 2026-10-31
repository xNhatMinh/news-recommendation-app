package com.example.newsrecommendation

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewModelScope
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.example.newsrecommendation.ui.navigation.AppNavHost
import com.example.newsrecommendation.ui.screens.home.HomeScreen
import com.example.newsrecommendation.ui.theme.NewsRecommendationTheme
import com.example.newsrecommendation.ui.viewmodel.HomeViewModel
import com.example.newsrecommendation.ui.viewmodel.SessionViewModel
import java.io.File
import java.util.UUID
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GuestNavigationTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var directory: File
    private lateinit var testApplication: Application
    private lateinit var session: SessionViewModel
    private var homeModel: HomeViewModel? = null

    @Before
    fun prepareGuest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        directory = File(context.cacheDir, "guest-session-${UUID.randomUUID()}")
        check(directory.mkdirs())
        // Dùng thư mục riêng; không đọc hoặc xóa phiên đăng nhập thật trên máy.
        testApplication = object : Application() {
            override fun getApplicationContext(): Context = this
            override fun getNoBackupFilesDir(): File = directory
        }
        compose.runOnUiThread { session = SessionViewModel(testApplication) }
    }

    @After
    fun cleanUp() {
        compose.runOnUiThread {
            session.viewModelScope.cancel()
            homeModel?.viewModelScope?.cancel()
        }
        directory.delete()
    }

    private fun showGuestApp() {
        compose.setContent {
            NewsRecommendationTheme(darkTheme = false, dynamicColor = false) {
                AppNavHost(session = session)
            }
        }
    }

    private fun assertGuestHome() {
        compose.onNodeWithText("Mới nhất").assertIsDisplayed()
        compose.onNodeWithText("Đăng nhập").assertIsDisplayed()
        compose.onNodeWithText("Dành cho bạn").assertDoesNotExist()
        compose.onNodeWithText("Tổng hợp").assertDoesNotExist()
        compose.onNodeWithContentDescription("Chủ đề yêu thích").assertDoesNotExist()
        compose.onNodeWithContentDescription("Thông báo").assertDoesNotExist()
        compose.onNodeWithContentDescription("Mở menu").assertDoesNotExist()
        compose.onNodeWithContentDescription("Tìm kiếm bài báo").assertIsDisplayed()
    }

    @Test
    fun guestCanSkipLoginAndReturnToNews() {
        showGuestApp()
        assertGuestHome()
        compose.onNodeWithText("Đăng nhập").performClick()
        compose.onNodeWithText("Tiếp tục với Google").assertIsDisplayed()
        compose.onNodeWithText("Tiếp tục đọc tin").assertIsDisplayed().performClick()
        assertGuestHome()
    }

    @Test
    fun systemBackFromLoginReturnsToNews() {
        showGuestApp()
        compose.onNodeWithText("Đăng nhập").performClick()
        compose.onNodeWithText("Tiếp tục đọc tin").assertIsDisplayed()
        Espresso.pressBack()
        assertGuestHome()
    }

    @Test
    fun googleSignInCanBeCancelledOrRetriedWithoutLeavingLogin() {
        showGuestApp()
        compose.onNodeWithText("Đăng nhập").performClick()
        compose.onNodeWithText("Tiếp tục với Google").assertIsDisplayed().performClick()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val appPackage = instrumentation.targetContext.packageName
        var providerOpened = false
        compose.waitUntil(timeoutMillis = 15_000) {
            val activePackage = instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString()
            providerOpened = activePackage != null && activePackage != appPackage
            providerOpened || runCatching {
                compose.onNodeWithText("Tiếp tục với Google").assertIsEnabled()
            }.isSuccess
        }
        // Chỉ mở và hủy giao diện chọn tài khoản, không chọn tài khoản của người dùng.
        if (providerOpened) {
            instrumentation.uiAutomation.performGlobalAction(
                android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
            )
        }
        compose.waitUntil(timeoutMillis = 10_000) {
            runCatching { compose.onNodeWithText("Tiếp tục với Google").assertIsEnabled() }.isSuccess
        }
        compose.onNodeWithText("Tiếp tục đọc tin").assertIsDisplayed()
        compose.onNodeWithText("Tiếp tục với Google").assertIsEnabled()
    }

    @Test
    fun forgotPasswordPrefillsEmailValidatesAndReturnsToLogin() {
        showGuestApp()
        compose.onNodeWithText("Đăng nhập").performClick()
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("reader@example.com")
        compose.onNodeWithText("Quên mật khẩu?").assertIsDisplayed().performClick()
        val dialogEmail = hasSetTextAction() and hasAnyAncestor(isDialog())
        compose.onNode(dialogEmail).assertTextContains("reader@example.com")
        compose.onNode(dialogEmail).performTextClearance()
        compose.onNodeWithText("Gửi liên kết").performClick()
        compose.onNodeWithText("Bạn chưa nhập email").assertIsDisplayed()
        compose.onNode(dialogEmail).performTextInput("invalid-email")
        compose.onNodeWithText("Gửi liên kết").performClick()
        compose.onNodeWithText("Email không hợp lệ").assertIsDisplayed()
        compose.onNodeWithText("Hủy").performClick()
        compose.onNode(isDialog()).assertDoesNotExist()
        compose.onAllNodes(hasSetTextAction())[0].assertTextContains("reader@example.com")
        compose.onNodeWithText("Tiếp tục với Google").assertIsEnabled()
    }

    @Test
    fun personalFeaturesAppearOnlyAfterSignIn() {
        var signedIn by mutableStateOf(false)
        compose.runOnUiThread { homeModel = HomeViewModel(testApplication) }
        compose.setContent {
            NewsRecommendationTheme(darkTheme = false, dynamicColor = false) {
                HomeScreen(
                    isLoggedIn = signedIn,
                    onLogoutClick = {}, onLoginClick = {}, onForYouLoginClick = {},
                    onSessionExpired = {}, openForYouAfterLogin = false, onForYouHandled = {},
                    onPreferencesClick = {}, onSummaryClick = {}, onNotificationsClick = {},
                    onSearchClick = {}, onSavedArticlesClick = {}, onArticleClick = {},
                    viewModel = homeModel!!
                )
            }
        }
        assertGuestHome()
        compose.runOnUiThread { signedIn = true }
        compose.onNodeWithText("Đăng nhập").assertDoesNotExist()
        compose.onNodeWithText("Dành cho bạn").assertIsDisplayed()
        compose.onNodeWithText("Tổng hợp").assertIsDisplayed()
        compose.onNodeWithContentDescription("Chủ đề yêu thích").assertIsDisplayed()
        compose.onNodeWithContentDescription("Thông báo").assertIsDisplayed()
        compose.onNodeWithContentDescription("Mở menu").performClick()
        compose.onNodeWithText("Bài đã lưu").assertIsDisplayed()
        Espresso.pressBack()
        compose.runOnUiThread { signedIn = false }
        assertGuestHome()
    }
}
