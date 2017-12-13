/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.view.list

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import vandyke.siamobile.R
import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.ui.renter.viewmodel.RenterViewModel
import vandyke.siamobile.util.GenUtil

class DirHolder(itemView: View) : NodeHolder(itemView) {
    val image: ImageView = itemView.findViewById(R.id.dirImage)
    val name: TextView = itemView.findViewById(R.id.dirName)
    val size: TextView = itemView.findViewById(R.id.dirSize)
    val more: ImageButton = itemView.findViewById(R.id.dirMore)

    fun bind(dir: SiaDir, viewModel: RenterViewModel) {
        name.text = dir.name
        size.text = GenUtil.readableFilesizeString(dir.size)
        itemView.setOnClickListener { v -> viewModel.changeDir(dir) }
        more.setOnClickListener {
            viewModel.displayDetails(dir)
        }
    }
}