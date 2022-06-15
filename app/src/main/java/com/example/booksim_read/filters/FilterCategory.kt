package com.example.booksim_read.filters

import android.widget.Filter
import com.example.booksim_read.models.ModelCategory
import com.example.booksim_read.adapters.AdapterCategory

class FilterCategory: Filter {

    //array list in which we want to search
    private val filterList: ArrayList<ModelCategory>

    //adapter on which filter need to be implemented
    private val adapterCategory: AdapterCategory

    //constructor
    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint =  constraint
        val results = FilterResults()

        //value should not be null and not empty
        if(constraint != null && constraint.isNotEmpty()){
            //searched value is not null not empty

            //change to upper case, or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase()
            val filteredModel: ArrayList<ModelCategory> = ArrayList()
            for (i in 0  until filterList.size){
                //validate
                if(filterList[i].category.toUpperCase().contains(constraint)){
                    //add to filter list
                    filteredModel.add(filterList[i])
                }
            }
            results.count = filteredModel.size
            results.values = filteredModel
        }
        else{
            //search value is either null or empty
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        //notify changes
        adapterCategory.notifyDataSetChanged()
    }


}