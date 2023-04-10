package com.xzentry.shorten.ui.base

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.xzentry.shorten.utils.NetworkUtils
import dagger.android.AndroidInjection


abstract class BaseActivity<T : ViewDataBinding, V : BaseViewModel<*>> : AppCompatActivity(), BaseFragment.Callback {

    // TODO
    // this can probably depend on isLoading variable of BaseViewModel,
    // since its going to be common for all the activities
    private var mProgressDialog: ProgressDialog? = null
    var viewDataBinding: T? = null
        private set
    private var mViewModel: V? = null

    /**
     * Override for set binding variable
     *
     * @return variable cat_id
     */
    abstract val bindingVariable: Int

    /**
     * @return layout resource cat_id
     */
    @get:LayoutRes
    abstract val layoutId: Int

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    abstract val viewModel: V

    val isNetworkConnected: Boolean
        get() = NetworkUtils.isNetworkConnected(applicationContext)


    override fun onFragmentAttached() {

    }

    override fun onFragmentDetached(tag: String) {

    }

    /* protected fun attachBaseContext(newBase: Context) {
         super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
     }*/

    override fun onCreate(savedInstanceState: Bundle?) {

        performDependencyInjection()
        super.onCreate(savedInstanceState)
        performDataBinding()
    }

    override fun onStart() {
        super.onStart()

    }

    fun hideKeyboard() {
        val view = this.getCurrentFocus()
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.hideSoftInputFromWindow(view!!.getWindowToken(), 0)
        }
    }

    fun hideLoading() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.cancel()
        }
    }

    fun performDependencyInjection() {
        AndroidInjection.inject(this)
    }

    fun showLoading() {
        hideLoading()
        // mProgressDialog = CommonUtils.showLoadingDialog(this)
    }

    private fun performDataBinding() {
        viewDataBinding = DataBindingUtil.setContentView(this, layoutId)
        this.mViewModel = if (mViewModel == null) viewModel else mViewModel
        viewDataBinding!!.setVariable(bindingVariable, mViewModel)
        viewDataBinding!!.executePendingBindings()
    }

    fun startSlideUpActivity(activityClass: Class<*>, flags: Int, extras: Bundle?) {
        val intent = Intent(this, activityClass)

        if (flags > -1) {
            intent.setFlags(flags)
        }

        if (extras != null) {
            intent.putExtras(extras)
        }

        /*val options = ActivityOptions.makeCustomAnimation(
            this, R.anim.slide_up, R.anim.do_nothing
        )*/
        startActivity(intent/*, options.toBundle()*/)
    }


}
