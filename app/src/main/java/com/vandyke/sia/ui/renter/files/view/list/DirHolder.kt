/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.arch.lifecycle.Observer
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import com.vandyke.sia.R
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.showDialogAndKeyboard

class DirHolder(itemView: View, val viewModel: FilesViewModel) : NodeHolder(itemView) {
    private val image: ImageView = itemView.findViewById(R.id.dirImage)
    private val name: TextView = itemView.findViewById(R.id.dirName)
    private val size: TextView = itemView.findViewById(R.id.dirSize)
    private val more: ImageButton = itemView.findViewById(R.id.dirMore)

    private val moreMenu = PopupMenu(itemView.context, more)

    lateinit var dir: Dir

    private val obs = Observer<List<Node>> {
        if (it?.find { it.path == dir.path} != null) {
            itemView.setBackgroundColor(selectedBg)
        } else {
            itemView.setBackgroundColor(normalBg)
        }
    }

    init {
        itemView.setOnClickListener {
            viewModel.changeDir(dir.path)
        }

        image.setOnClickListener {
            viewModel.toggleSelect(dir)
        }

        more.setOnClickListener {
            moreMenu.show()
        }

        moreMenu.inflate(R.menu.dir_menu)
        moreMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.dirDelete -> viewModel.deleteDir(dir)
                R.id.dirRename -> {
                    val dialogView = View.inflate(itemView.context, R.layout.edit_text_field, null)
                    AlertDialog.Builder(itemView.context)
                            .setTitle("Rename ${dir.name}")
                            .setView(dialogView)
                            .setPositiveButton("Rename", { _, _ ->
                                viewModel.renameDir(dir, dialogView.findViewById<EditText>(R.id.field).text.toString())
                            })
                            .setNegativeButton("Cancel", null)
                            .showDialogAndKeyboard()
                }
                R.id.dirDownload -> TODO()
            }
            true
        }
    }

    fun bind(dir: Dir) {
        this.dir = dir
        viewModel.selectedNodes.removeObserver(obs)
        viewModel.selectedNodes.observeForever(obs)
        name.text = dir.name
        size.text = GenUtil.readableFilesizeString(dir.size)
    }
}