package science.apolline.view.activity

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.preference.*
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main_content.*
import org.w3c.dom.Text
import science.apolline.R
import science.apolline.service.database.AppDatabase
import science.apolline.utils.QueryBDDAsyncTask
import science.apolline.utils.QuerySynchro
import java.lang.ref.WeakReference
import java.util.*


/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || GeneralPreferenceFragment::class.java.name == fragmentName
                || ChartPreferenceFragment::class.java.name == fragmentName
                || DataSyncPreferenceFragment::class.java.name == fragmentName
                || NotificationPreferenceFragment::class.java.name == fragmentName
                || DataErasePreferenceFragment::class.java.name == fragmentName
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sensor_name"), STRING)
            bindPreferenceSummaryToValue(findPreference("sensor_mac_address"), STRING)
            bindPreferenceSummaryToValue(findPreference("device_uuid"), STRING)
//            bindPreferenceSummaryToValue(findPreference("example_list"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class NotificationPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_notification)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"), STRING)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class ChartPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_chart)
            setHasOptionsMenu(true)

            bindPreferenceSummaryToValue(findPreference("visible_entries"), INTEGER)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }


    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class DataSyncPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_data_sync)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_mod"), STRING)
            bindPreferenceSummaryToValue(findPreference("sync_frequency"), STRING)
            bindPreferenceSummaryToValue(findPreference("collect_data_frequency"), STRING)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class DataErasePreferenceFragment : PreferenceFragment(){

        lateinit var nbDataStoreTxt : TextView
        lateinit var nbDataSynchronizedTxt : TextView
        lateinit var nbDataUnsynchronizedTxt : TextView
        lateinit var dateLastSyncTxt : TextView


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

        }

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
            if (inflater != null) {
                var view = inflater.inflate(R.layout.fragment_delete_synchronized, container, false)

                setTextView(view)

                var btnDeleteData = view.findViewById<Button>(R.id.button_delete_synchonized_data)
                btnDeleteData.setOnClickListener{ view ->
                   showDeleteDataDialog()
                }

                return view
            }
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        private fun showDeleteDataDialog() {
            val builder = AlertDialog.Builder(this.view.context)
            var countSyncData = 0

            if (this.view.context != null) {
                 countSyncData = QueryBDDAsyncTask(this).execute("getSensorSyncCount").get()
            }

            // Set the alert dialog title
            builder.setTitle("Delete synchronized data")

            // Display a message on alert dialog
            builder.setMessage("Do you want to delete all the synchronized data ? (" +
                    countSyncData + ")")

            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton("YES"){dialog, which ->
                // Do something when user press the positive button
                QueryBDDAsyncTask(this).execute("deleteDataSync").get()
                Toasty.info(this.view.context,"Synchonized data has been deleted",Toast.LENGTH_LONG,true).show()

                setTextView(this.view)
            }

            // Display a neutral button on alert dialog
            builder.setNeutralButton("Cancel"){_,_ ->
                Toasty.info(this.view.context,"Synchronized data has been kept",Toast.LENGTH_LONG,true).show()
            }

            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()

            // Display the alert dialog on app interface
            dialog.show()
        }

        private fun setTextView(view: View){
            nbDataStoreTxt = view.findViewById<TextView>(R.id.nb_data_saved)
            var nbDataStore = QueryBDDAsyncTask(this).execute("getSensorCount").get()
            nbDataStoreTxt.setText(nbDataStore.toString())

            nbDataSynchronizedTxt = view.findViewById<TextView>(R.id.nb_data_sync)
            var nbDataSynchronized = QueryBDDAsyncTask(this).execute("getSensorSyncCount").get()
            nbDataSynchronizedTxt.setText(nbDataSynchronized.toString())

            nbDataUnsynchronizedTxt = view.findViewById<TextView>(R.id.nb_data_unsync)
            var nbDataUnsynchronized = QueryBDDAsyncTask(this).execute("getSensorNotSyncCount").get()
            nbDataUnsynchronizedTxt.setText(nbDataUnsynchronized.toString())

            dateLastSyncTxt = view.findViewById<TextView>(R.id.date_last_sync)
            var dateLastSync = QuerySynchro(this).execute("getLastSync").get()
            dateLastSyncTxt.setText(Date(dateLastSync).toString())

            Toast.makeText(view.context, dateLastSync.toString(), Toast.LENGTH_LONG).show()
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val index = preference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null)

            } else if (preference is RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent)

                } else {
                    val ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue))

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null)
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        val name = ringtone.getTitle(preference.getContext())
                        preference.setSummary(name)
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference, type: String) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            when (type) {
                STRING -> {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
                }

                INTEGER -> {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getInt(preference.key, 0))
                }
            }

        }

        private const val INTEGER = "Integer"
        private const val STRING = "String"

    }
}
