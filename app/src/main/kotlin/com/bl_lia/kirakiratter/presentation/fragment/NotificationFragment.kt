package com.bl_lia.kirakiratter.presentation.fragment

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bl_lia.kirakiratter.App
import com.bl_lia.kirakiratter.R
import com.bl_lia.kirakiratter.domain.entity.Notification
import com.bl_lia.kirakiratter.domain.entity.Status
import com.bl_lia.kirakiratter.domain.extension.preparedErrorMessage
import com.bl_lia.kirakiratter.domain.value_object.Translation
import com.bl_lia.kirakiratter.presentation.activity.AccountActivity
import com.bl_lia.kirakiratter.presentation.activity.KatsuActivity
import com.bl_lia.kirakiratter.presentation.adapter.notification.NotificationAdapter
import com.bl_lia.kirakiratter.presentation.internal.di.component.DaggerNotificationComponent
import com.bl_lia.kirakiratter.presentation.internal.di.component.NotificationComponent
import com.bl_lia.kirakiratter.presentation.internal.di.module.FragmentModule
import com.bl_lia.kirakiratter.presentation.presenter.NotificationPresenter
import com.bl_lia.kirakiratter.presentation.scroll_listener.NotificationScrollListener
import com.trello.rxlifecycle2.components.support.RxFragment
import kotlinx.android.synthetic.main.fragment_notification.*
import javax.inject.Inject

class NotificationFragment : RxFragment(), ScrollableFragment {

    companion object {
        fun newInstance(): NotificationFragment = NotificationFragment()
    }

    @Inject
    lateinit var presenter: NotificationPresenter

    private var moreLoading: Boolean = false
    private var layoutManager: RecyclerView.LayoutManager? = null

    private val component: NotificationComponent by lazy {
        DaggerNotificationComponent.builder()
                .applicationComponent((activity.application as App).component)
                .fragmentModule(FragmentModule(this))
                .build()
    }

    private val adapter: NotificationAdapter by lazy {
        NotificationAdapter()
    }

    private val scrollListener: NotificationScrollListener by lazy {
        object: NotificationScrollListener(layoutManager as LinearLayoutManager) {
            override fun onLoadMore() {
                adapter.maxId?.let { maxId ->
                    if (moreLoading) return@let
                    moreLoading = true
                    presenter.fetchMoreNotification(maxId)
                            ?.doAfterTerminate {
                                moreLoading = false
                            }
                            ?.subscribe { list, error ->
                                if (error != null) {
                                    showError(error)
                                    return@subscribe
                                }

                                adapter.add(list)
                            }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_notification, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (list_notification.layoutManager == null) {
            layoutManager = LinearLayoutManager(activity)
            list_notification.layoutManager = layoutManager
        }

        list_notification.adapter = adapter
        list_notification.addOnScrollListener(scrollListener)

        layout_swipe_refresh?.setOnRefreshListener {
            fetch()
        }

        adapter.onClickAccount.subscribe { pair ->
            val account = pair.first
            val imageView = pair.second
            val intent = Intent(activity, AccountActivity::class.java).apply {
                putExtra(AccountActivity.INTENT_PARAM_ACCOUNT, account)
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageView, "avatar")
            startActivity(intent, options.toBundle())
        }

        adapter.onClickReply.subscribe { status ->
            val target = status.reblog ?: status
            val intent = Intent(activity, KatsuActivity::class.java).apply {
                putExtra(KatsuActivity.INTENT_PARAM_REPLY_ACCOUNT_NAME, target.account?.userName)
                putExtra(KatsuActivity.INTENT_PARAM_REPLY_STATUS_ID, target.id)
            }
            startActivity(intent)
        }

        adapter.onClickReblog.subscribe { notification ->
            val target = notification.status?.reblog ?: notification.status
            target?.let { target ->
                presenter.reblog(target)
                        .compose(bindToLifecycle())
                        .subscribe { status, error ->
                            if (error != null) {
                                showError(error)
                                return@subscribe
                            }

                            adapter.update(notification.copy(status = status))
                        }
            }
        }

        adapter.onClickFavourite.subscribe { notification ->
            notification.status?.let { status ->
                presenter.favourite(status)
                        .compose(bindToLifecycle())
                        .subscribe { status, error ->
                            if (error != null) {
                                showError(error)
                                return@subscribe
                            }

                            adapter.update(notification.copy(status = status))
                        }
            }
        }

        adapter.onClickTranslate.subscribe { notification ->
                presenter.translate(notification)
        }

        fetch()
    }

    override fun scrollToTop() {
        list_notification?.smoothScrollToPosition(0)
    }

    fun tranlateText(notification: Notification, status: Status, translations: List<Translation>, error: Throwable?) {
        if (error != null) {
            showError(error)
            return
        }
        adapter.addTranslatedText(notification.copy(status = status), translations.first().translatedText)
    }

    private fun fetch() {
        layout_swipe_refresh?.isRefreshing = true
        presenter.fetchNotification()
                .doAfterTerminate {
                    layout_swipe_refresh?.isRefreshing = false
                }
                .compose(bindToLifecycle())
                .subscribe { list, error ->
                    if (error != null) {
                        showError(error)
                        return@subscribe
                    }

                    adapter.reset(list)
                }
    }

    private fun showError(error: Throwable) {
        Snackbar.make(layout_content, error.preparedErrorMessage(activity), Snackbar.LENGTH_LONG).show()
    }
}