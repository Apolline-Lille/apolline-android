//package science.apolline.view.fragment
//
//
//import android.app.Activity
//import android.app.DatePickerDialog
//import android.app.Dialog
//import android.content.Intent
//import android.os.Bundle
//import android.support.v4.app.DialogFragment
//import android.widget.Toast
//
//import java.util.Calendar
//
//class MyDatePickerFragment : DialogFragment() {
//
//    private val dateSetListener = { view, year, month, day ->
//        val date = view.getDayOfMonth() +
//                "/" + (view.getMonth() + 1) +
//                "/" + view.getYear()
//        Toast.makeText(activity, "selected date is $date", Toast.LENGTH_SHORT).show()
//        val bundle = Bundle()
//        bundle.putString(SELECTED_DATE, date)
//        val intent = Intent().putExtras(bundle)
//
//        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
//    }
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//
//        val c = Calendar.getInstance()
//        val year = c.get(Calendar.YEAR)
//        val month = c.get(Calendar.MONTH)
//        val day = c.get(Calendar.DAY_OF_MONTH)
//
//        return DatePickerDialog(activity!!, dateSetListener, year, month, day)
//    }
//
//    companion object {
//
//        val SELECTED_DATE = "com.sem.lamoot.elati.danstonplacard.danstonplacard.view.fragment.SELECTED_DATE"
//    }
//}