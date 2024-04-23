package ru.diverstat.discoin

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import ru.diverstat.discoin.databinding.ItemViewBinding

class ItemsListViewAdapter(
    private val activity: Activity,
    itemsList: MutableList<WalletItem>,
) :
    BaseAdapter() {
    private var itemsList: MutableList<WalletItem> = itemsList

    override fun getCount(): Int {
        return itemsList.size
    }

    override fun getItem(i: Int): Any {
        return i
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(i: Int, convertView: View?, viewGroup: ViewGroup): View {
        val vi = ItemViewBinding.inflate(activity.layoutInflater)

        vi.name.text = itemsList[i].name
        vi.count.text = itemsList[i].count.toString()
        vi.profit.text = (itemsList[i].profit * itemsList[i].count).toString()
        vi.cost.text = "${formatter(itemsList[i].cost / 1_000_000)}.${(itemsList[i].cost % 1_000_000).toString().padStart(6, '0')}"

        if (itemsList[i].count == 0L) {
            vi.root.background.setTint(Color.parseColor("#B5B5B5"))
        } else if (itemsList[i].count == itemsList[i].max) {
            vi.root.background.setTint(Color.parseColor("#03A9F4"))
        } else {
            vi.root.background.setTint(Color.parseColor("#4CAF50"))
        }

        return vi.root
    }
}