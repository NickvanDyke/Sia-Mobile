/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import kotlinx.android.synthetic.main.fragment_wallet.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.data.consensus.ConsensusData
import vandyke.siamobile.backend.data.wallet.ScPriceData
import vandyke.siamobile.backend.data.wallet.TransactionsData
import vandyke.siamobile.backend.data.wallet.WalletData
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.backend.siad.SiadService
import vandyke.siamobile.prefs
import vandyke.siamobile.ui.MainActivity
import vandyke.siamobile.ui.wallet.model.IWalletModel
import vandyke.siamobile.ui.wallet.model.WalletModelColdStorage
import vandyke.siamobile.ui.wallet.model.WalletModelHttp
import vandyke.siamobile.ui.wallet.presenter.IWalletPresenter
import vandyke.siamobile.ui.wallet.presenter.WalletPresenter
import vandyke.siamobile.ui.wallet.view.dialogs.*
import vandyke.siamobile.ui.wallet.view.transactionslist.TransactionAdapter
import vandyke.siamobile.util.*
import java.math.BigDecimal

class WalletFragment : Fragment(), IWalletView, SiadService.SiadListener {

    private lateinit var model: IWalletModel
    private lateinit var presenter: IWalletPresenter
    private var cachedMode: String = "none"

    private val adapter = TransactionAdapter()

    private var statusButton: MenuItem? = null
    private var walletData: WalletData? = null // TODO: temp fix for tracking status to set icon
    private var balanceHastings: BigDecimal = BigDecimal.ZERO

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /* color stuff depending on theme */
        if (MainActivity.appTheme === MainActivity.Theme.AMOLED || MainActivity.appTheme === MainActivity.Theme.CUSTOM) {
            top_shadow.visibility = View.GONE
        } else if (MainActivity.appTheme === MainActivity.Theme.DARK) {
            top_shadow.setBackgroundResource(R.drawable.top_shadow_dark)
        }
        if (MainActivity.appTheme === MainActivity.Theme.AMOLED) {
            receiveButton.setBackgroundColor(android.R.color.transparent)
            sendButton.setBackgroundColor(android.R.color.transparent)
        }
        syncBar.setProgressTextColor(MainActivity.defaultTextColor)

        /* set up recyclerview for transactions */
        val layoutManager = LinearLayoutManager(activity)
        transactionList.layoutManager = layoutManager
        transactionList.addItemDecoration(DividerItemDecoration(transactionList.context, layoutManager.orientation))
        transactionList.adapter = adapter

        /* set up click listeners for the big stuff */
        sendButton.setOnClickListener { fillExpandableFrame(WalletSendDialog(presenter)) }
        receiveButton.setOnClickListener { fillExpandableFrame(WalletReceiveDialog(model)) }
        balanceText.setOnClickListener { v ->
            GenUtil.getDialogBuilder(v.context)
                    .setTitle("Exact Balance")
                    .setMessage("${balanceHastings.toSC().toPlainString()} Siacoins")
                    .setPositiveButton("Close", null)
                    .show()
        }

        /* set listener to refresh the presenter when the swipelayout is triggered */
        transactionListSwipe.setOnRefreshListener { presenter.refresh() }

        /* listen to siad output, so that we can refresh the presenter at appropriate times */
        SiadService.addListener(this)
    }

    override fun onSuccess() {
        SnackbarUtil.successSnackbar(view)
    }

    override fun onWalletUpdate(walletData: WalletData) {
        this.balanceHastings = walletData.confirmedsiacoinbalance
        this.walletData = walletData
        balanceUnconfirmed?.text = "${if (walletData.unconfirmedsiacoinbalance > BigDecimal.ZERO) "+" else ""}${walletData.unconfirmedsiacoinbalance.toSC().round().toPlainString()} unconfirmed"
        balanceText?.text = walletData.confirmedsiacoinbalance.toSC().round().toPlainString()
        setStatusIcon()
        transactionListSwipe?.isRefreshing = false
    }

    override fun onUsdUpdate(scPriceData: ScPriceData) {
        balanceUsdText?.text = "${balanceHastings.toSC().toUsd(scPriceData.price_usd).round().toPlainString()} USD"
    }

    override fun onTransactionsUpdate(transactionsData: TransactionsData) {
        val hideZero = prefs.hideZero
        adapter.transactions = transactionsData.alltransactions.filterNot { hideZero && it.isNetZero }.reversed()
        adapter.notifyDataSetChanged()
    }

    override fun onConsensusUpdate(consensusData: ConsensusData) {
        if (consensusData.synced) {
            syncText?.text = "${getString(R.string.synced)}: ${consensusData.height}"
            syncBar?.progress = 100
        } else {
            syncText?.text = "${getString(R.string.syncing)}: ${consensusData.height}"
            syncBar?.progress = consensusData.syncprogress.toInt()
        }
    }

    override fun onError(error: SiaError) {
        error.snackbar(view)
    }

    override fun onWalletError(error: SiaError) {
        error.snackbar(view)
        transactionListSwipe?.isRefreshing = false
    }

    override fun onUsdError(error: SiaError) {
        SnackbarUtil.snackbar(view, "Error retrieving USD value", Snackbar.LENGTH_SHORT)
    }

    override fun onTransactionsError(error: SiaError) {
        error.snackbar(view)
    }

    override fun onConsensusError(error: SiaError) {
        error.snackbar(view)
        syncText?.text = getString(R.string.not_synced)
        syncBar?.progress = 0
    }

    override fun onSiadOutput(line: String) {
        if (line.contains("Finished loading") || line.contains("Done!"))
            presenter.refresh()
    }

    override fun onWalletCreated(seed: String) {
        if (prefs.operationMode == "cold_storage")
            WalletCreateDialog.showCsWarning(context!!)
        WalletCreateDialog.showSeed(seed, context!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionStatus -> {
//                when (statusButton?.icon?.constantState) {
//                    activity.resources.getDrawable(R.drawable.ic_add, null).constantState -> fillExpandableFrame(WalletCreateDialog(presenter))
//                    activity.resources.getDrawable(R.drawable.ic_lock_outline, null).constantState -> fillExpandableFrame(WalletUnlockDialog(presenter))
//                    activity.resources.getDrawable(R.drawable.ic_lock_open, null).constantState -> presenter.lock()
//                }
                when (walletData?.encrypted) {
                    false -> fillExpandableFrame(WalletCreateDialog(presenter))
                    true -> if (!walletData!!.unlocked) fillExpandableFrame(WalletUnlockDialog(presenter))
                    else presenter.lock()
                }
            }
            R.id.actionUnlock -> fillExpandableFrame(WalletUnlockDialog(presenter))
            R.id.actionLock -> presenter.lock()
            R.id.actionChangePassword -> fillExpandableFrame(WalletChangePasswordDialog(presenter))
            R.id.actionViewSeeds -> fillExpandableFrame(WalletSeedsDialog(model))
            R.id.actionCreateWallet -> fillExpandableFrame(WalletCreateDialog(presenter))
            R.id.actionSweepSeed -> fillExpandableFrame(WalletSweepSeedDialog(presenter))
            R.id.actionViewAddresses -> fillExpandableFrame(WalletAddressesDialog(model))
            R.id.actionGenPaperWallet -> (activity as MainActivity).displayFragmentClass(PaperWalletFragment::class.java, "Generated paper wallet", null)
        }

        return super.onOptionsItemSelected(item)
    }

    fun fillExpandableFrame(fragment: Fragment) {
        fragmentManager!!.beginTransaction().replace(R.id.expandableFrame, fragment).commit()
        expandableFrame?.visibility = View.VISIBLE
    }

    override fun closeExpandableFrame() {
        expandableFrame.visibility = View.GONE
        GenUtil.hideSoftKeyboard(activity)
    }

    override fun onResume() {
        super.onResume()
        if (prefs.operationMode != cachedMode) {
            cachedMode = prefs.operationMode
            model = if (cachedMode == "cold_storage") WalletModelColdStorage() else WalletModelHttp()
            presenter = WalletPresenter(this, model)
        }
        presenter.refresh()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            activity!!.invalidateOptionsMenu()
            if (prefs.operationMode != cachedMode) {
                cachedMode = prefs.operationMode
                model = if (cachedMode == "cold_storage") WalletModelColdStorage() else WalletModelHttp()
                presenter = WalletPresenter(this, model)
            }
            presenter.refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SiadService.removeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu)
        statusButton = menu.findItem(R.id.actionStatus)
        if (walletData != null)
            setStatusIcon()
    }

    fun setStatusIcon() {
        if (walletData != null)
            when (walletData!!.encrypted) {
                false -> statusButton?.setIcon(R.drawable.ic_add)
                true -> if (!walletData!!.unlocked) statusButton?.setIcon(R.drawable.ic_lock_outline)
                else statusButton?.setIcon(R.drawable.ic_lock_open)
            }
    }

    fun onBackPressed(): Boolean {
        if (expandableFrame.visibility == View.VISIBLE) {
            closeExpandableFrame()
            return true
        } else {
            return false
        }
    }
}
