/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.fragments;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vandyke.siamobile.R;
import vandyke.siamobile.misc.TextTouchCopyListAdapter;
import vandyke.siamobile.misc.Utils;

import java.util.ArrayList;

public class PaperWalletFragment extends Fragment {

    @BindView(R.id.paperSeed) public TextView seedText;
    @BindView(R.id.paperAddresses) public ListView addressList;
    @BindView(R.id.paperCopy) public Button copyAll;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_paper_wallet, null);
        ButterKnife.bind(this, view);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ArrayList<String> walletInfo = Utils.getNewWallet(getActivity());
        if (walletInfo == null) {
            seedText.setText("Failed to generate paper wallet");
            return;
        }

        final String seed = walletInfo.remove(0);
        seedText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("paper wallet info", seed);
                clipboard.setPrimaryClip(clip);
                Utils.snackbar(v, "Copied seed to clipboard", Snackbar.LENGTH_SHORT);
            }
        });
        seedText.setText(seed);

        addressList.setAdapter(new TextTouchCopyListAdapter(getActivity(), R.layout.text_touch_copy_list_item, walletInfo));

        copyAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StringBuilder result = new StringBuilder();
                result.append("Seed:\n");
                result.append(seed + "\n");
                result.append("Addresses:\n");
                for (int i = 0; i < walletInfo.size(); i++) {
                    result.append(walletInfo.get(i));
                    if (i < walletInfo.size() - 1)
                        result.append(",\n");
                }
                ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("paper wallet info", result.toString());
                clipboard.setPrimaryClip(clip);
                Utils.snackbar(v, "Copied seed and addresses to clipboard", Snackbar.LENGTH_SHORT);
            }
        });
    }
}