package com.example.booksim_read

import android.widget.Filter
import java.util.logging.LogRecord

class FilterPdfUser :Filter {

    //arraylist in which we want to search
    var filterList: ArrayList<ModelPdf>
    //adapter in which filter need to be implemented
    var adapterPdfUser: AdapterPdfUser

    //constructor
    constructor(filterLise: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) {
        this.filterList = filterLise
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constaint: CharSequence?): FilterResults {
        var constaint: CharSequence? = constaint
        val results = FilterResults()
        //value to be search should not be null and not empty
        if(constaint != null && constaint.isNotEmpty()){
            //not null nor empty

            //change upper case or lower case to remove case sensitivity
            constaint = constaint.toString().toUpperCase()
            val filteredModels = ArrayList<ModelPdf>()
            for(i in filterList.indices){
                if(filterList[i].title.toUpperCase().contains(constaint)){
                    //search vaule matched with title and add to list
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results. values = filteredModels
        }
        else{
            //either it is null or is empty
            //return orginal list and sizez
            results.count = filterList.size
            results.values = filterList

        }
        return results

    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        //applay filter changes
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfUser.notifyDataSetChanged()
    }


}