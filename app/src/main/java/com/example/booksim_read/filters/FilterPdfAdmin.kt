package com.example.booksim_read.filters

import android.widget.Filter
import com.example.booksim_read.models.ModelPdf
import com.example.booksim_read.adapters.AdapterPdfAdmin


/*Used to filter data from recycleview | search pdf from list in recyclerview*/
class FilterPdfAdmin: Filter {
    //arraylist in which we want to be implemented
    var filterList: ArrayList<ModelPdf>

    //adapter in which filter need to be implemented
    var adapterPdfAdmin: AdapterPdfAdmin

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint //value to search
        val results = FilterResults()
        //value to be search should not be null and not empty
        if(constraint != null && constraint.isNotEmpty()){
            //change to upper case, or lowecse to avoid case sensitivity
                constraint = constraint.toString().toLowerCase()
            val filterModels = ArrayList<ModelPdf>()
            for(i in filterList.indices){
                //validate if match
                if(filterList[i].title.toLowerCase().contains(constraint)){
                    //search value is similar to value in list, add to filtered list
                    filterModels.add(filterList[i])
                }
            }
            results.count = filterModels.size
            results.values = filterModels
        }
        else{
            //search value is either null or empty, return all data
            results.count = filterList.size
            results.values = filterList
        }
        return  results
    }

    override fun publishResults(p0: CharSequence, results: FilterResults) {
        //apply filter changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfAdmin.notifyDataSetChanged()
    }


}