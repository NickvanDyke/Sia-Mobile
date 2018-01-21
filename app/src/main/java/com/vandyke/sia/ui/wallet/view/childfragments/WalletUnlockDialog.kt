/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.os.Bundle
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.util.GenUtil
import kotlinx.android.synthetic.main.fragment_wallet_unlock.*

class WalletUnlockDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_unlock

    override fun create(view: View, savedInstanceState: Bundle?) {
        walletUnlockConfirm.setOnClickListener {
            viewModel.unlock(walletPassword.text.toString())
        }

        walletPassword.setOnEditorActionListener { v, actionId, event ->
            viewModel.unlock(walletPassword.text.toString())
            true
        }

        walletPassword.requestFocus()
        GenUtil.showSoftKeyboard(context!!)
    }
}