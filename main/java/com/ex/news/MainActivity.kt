package com.ex.news

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.nkzawa.socketio.client.Ack
import com.github.nkzawa.socketio.client.Socket
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(){

    lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var gson: Gson
    private lateinit var mSocket: Socket
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var newsRecycler: RecyclerView
    private lateinit var nativeAdLoader: AdLoader
    private lateinit var refreshLayout: LinearLayoutCompat
    private lateinit var newsRecyclerAdapter: NewsRecyclerAdapter
    private var articlesLoaded = false
    private var adsLoaded = 0
    private lateinit var readIntent: Intent
    private var instagram: Boolean = false
    private var pendingLogin: String? = null
    private lateinit var uri: Uri
    private lateinit var channel: NotificationChannel
    private lateinit var contentLayoutToolbarImage: AppCompatTextView
    private lateinit var notificationBadge: AppCompatTextView
    private lateinit var mainActivityToggle: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?){

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // blah blah blah
        notificationBadge = findViewById(R.id.notificationBadge)
        refreshLayout = findViewById(R.id.refreshLayout)
        mainActivityToggle = findViewById(R.id.mainActivityToggle)

        registerReceiver(connectivityChange, IntentFilter(BROADCAST_ACTION))

        databaseHelper = DatabaseHelper(this)
        uri = Uri.parse("http://instagram.com/")

       if(databaseHelper.isAppOutdated){

            startActivity(Intent(this@MainActivity, ActivityOutdated::class.java))
            finish()

        }

        connectionListener()

        MobileAds.initialize(this){

        }

        val igIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/"))
        igIntent.setPackage(getString(R.string.INSTAGRAM))

        if(isIntentAvailable(this@MainActivity, igIntent)){

            instagram = true
        }

       /* if(!VlogControlCenter.verifyInstaller(this)){

           startActivity(Intent(this, ActivityInvalidInstaller::class.java))
           finish()

        } */

        newsRecycler = findViewById(R.id.contentLayoutRecycler)
        val linearLayoutManager = LinearLayoutManager(this)
        newsRecycler.layoutManager = linearLayoutManager

        val articlesCached = databaseHelper.cachedArticles

        articlesCached.sortByDescending {

            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(it.publishDate)
        }

        newsRecycler.adapter = NewsRecyclerAdapter((articlesCached as MutableList<StoriesAdapterItem>)){ one ->

            if(one.javaClass.simpleName == VlogArticle::class.simpleName){

                val article = (one)

                readIntent = Intent(this@MainActivity, ReadActivity::class.java)
                readIntent.putExtra("article", gson.toJson(article))
                startActivityForResult(readIntent, READ_REQUEST_CODE)

            }
        }

        newsRecyclerAdapter = (newsRecycler.adapter as NewsRecyclerAdapter)
        // release memory
       // articlesCached.clear()

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        drawerLayout.setStatusBarBackground(Color.TRANSPARENT)

        setSupportActionBar(findViewById(R.id.toolbar))
        toggle = object: ActionBarDrawerToggle(this, drawerLayout, findViewById(R.id.toolbar), R.string.app_name, R.string.app_name){

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)

                findViewById<CoordinatorLayout>(R.id.cl).translationX = slideOffset * drawerView.width

            }
        }

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        gson = Gson()

        mSocket = SocketInstance().SocketInstance

        mSocket.on("authenticate"){

            (it[it.size-1] as Ack).call(gson.toJson(Authenticate(databaseHelper.username, databaseHelper.password, this.packageManager.getPackageInfo(this.packageName, 0).versionName)))

        }

        mSocket.on("needsUpdate"){

            runOnUiThread{

                databaseHelper.isAppOutdated = true
                startActivity(Intent(this@MainActivity, ActivityOutdated::class.java))
                finish()

            }
        }

        mSocket.on("notification"){

            runOnUiThread {

                val notification = gson.fromJson(it[0].toString(), NotificationContainer::class.java)
                databaseHelper.notification = notification.notifications.toMutableList()

                notificationBadge.visibility = View.VISIBLE
                notificationBadge.text = databaseHelper.notification.size.toString()

                findViewById<AppCompatImageView>(R.id.newNotification).visibility = View.VISIBLE

                showNotification("Hello there", "One new notification", Intent(this@MainActivity, ActivityNotification::class.java).apply {

                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            }

        }

        mSocket.on("serverToast"){
            runOnUiThread {
                Toast.makeText(this, it[0].toString(), Toast.LENGTH_SHORT).show()
            }
        }

        mSocket.on("serverAlert"){

            runOnUiThread {
                CustomDialogs.alert(this, it[0].toString(), "Got it"){
                    (it[it.size-1] as Ack).call("true")
                }
            }
        }

        mSocket.on("serverConfirm"){ ack ->

            runOnUiThread {
                CustomDialogs.confirm(this, ack[0].toString(), "Yes", "Cancel"){

                    if(it){
                        (ack[ack.size-1] as Ack).call("true")
                    }else{
                        (ack[ack.size-1] as Ack).call("false")
                    }
                }

            }

        }

        findViewById<AppCompatButton>(R.id.notificationButton).setOnClickListener {

            startActivity(Intent(this@MainActivity, ActivityNotification::class.java))

            Handler().postDelayed({
                drawerLayout.closeDrawer(GravityCompat.START)
            }, 500)
        }
        findViewById<AppCompatButton>(R.id.savedButton).setOnClickListener {

            startActivityForResult(Intent(this@MainActivity, ActivitySaved::class.java), SAVED_REQUEST_CODE)

            Handler().postDelayed({
                drawerLayout.closeDrawer(GravityCompat.START)
            }, 500)

        }
        findViewById<AppCompatButton>(R.id.chatButton).setOnClickListener {

            drawerLayout.closeDrawer(GravityCompat.START)

            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage(getString(R.string.INSTAGRAM))

            if(instagram){

                startActivity(intent)
            }else{

                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }

        }

        val videoOptions = VideoOptions.Builder()
            .setStartMuted(false)
            .setCustomControlsRequested(true)
            .setClickToExpandRequested(true)
            .build()

        val nativeAdOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        nativeAdLoader = AdLoader.Builder(this, "ca-app-pub-7545958636172314/5267332109")
            .forUnifiedNativeAd {

                adsLoaded += 1
                newsRecyclerAdapter.addRandomPosition(listOf(NativeAd(it)).toMutableList(), adsLoaded)

                if(!nativeAdLoader.isLoading && adsLoaded > 1){

                    if((newsRecyclerAdapter.items.size - adsLoaded) / adsLoaded > 3){

                        val toLoad = ((newsRecyclerAdapter.items.size - adsLoaded) / 3)

                        if(toLoad > 10){
                            nativeAdLoader.loadAds(AdRequest.Builder().build(), 10)
                        }else{
                            nativeAdLoader.loadAds(AdRequest.Builder().build(), toLoad)
                        }

                    }else{

                        findViewById<LinearLayoutCompat>(R.id.refreshLayout).visibility = View.GONE

                        when {

                            databaseHelper.canRead.isNullOrEmpty() -> {

                                val dateFromLast = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                                databaseHelper.canRead = dateFromLast

                            }

                            else -> {

                                if(databaseHelper.canRead != "#"){

                                    val dateInLocal = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(databaseHelper.canRead!!)
                                    val hrs = ((((Date().time - dateInLocal!!.time)/1000)/60)/60)
                                    val days = hrs/24

                                    if(days >= 3){

                                        CustomDialogs.rate(this){ a ->

                                            when(a){
                                                0 -> {

                                                    reviewApp()
                                                }

                                                1 -> {

                                                    reviewLater()
                                                }

                                                2 -> {

                                                    neverReview()
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }

                if(isDestroyed){
                    it.destroy()
                    return@forUnifiedNativeAd
                }
            }
            .withAdListener(object: AdListener(){
                override fun onAdFailedToLoad(p0: Int) {

                    if(!nativeAdLoader.isLoading){

                        findViewById<LinearLayoutCompat>(R.id.refreshLayout).visibility = View.GONE

                    }
                }

                override fun onAdImpression() {}
            })
            .withNativeAdOptions(nativeAdOptions)
            .build()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val name = getString(R.string.app_name)
            val descriptionText = getString(R.string.CHANNEL_DESCRIPTION)
            val importance = NotificationManager.IMPORTANCE_HIGH

            channel = NotificationChannel(name, name, importance).apply {

                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)

        }

    }
 
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.mn, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.rate -> {

                reviewApp()
            }

            R.id.share -> {

                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT,"Get latest news daily for free https://play.google.com/store/apps/details?id=com.ace.vlog")
                startActivity(Intent.createChooser(shareIntent,"Share link via"))
            }

            R.id.exit -> {

                finishAffinity()
            }

        }

        return true
    }

    override fun onBackPressed() {

        when{
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {

                drawerLayout.closeDrawer(GravityCompat.START)
            }
            else -> {
                super.onBackPressed()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            if(mSocket.connected()){

                login(data?.getStringExtra("login")!!)

            }else{

                pendingLogin = data?.getStringExtra("login")
            }

        }else{

            newsRecyclerAdapter.notifyDataSetChanged()

        }

    }

    override fun onResume() {
        super.onResume()

        val n = databaseHelper.notification

        if(n.size > 0){

            notificationBadge.visibility = View.VISIBLE
            notificationBadge.text = n.size.toString()

            findViewById<AppCompatImageView>(R.id.newNotification).visibility = View.VISIBLE

        }else{

            findViewById<AppCompatTextView>(R.id.notificationBadge).visibility = View.GONE
            findViewById<AppCompatImageView>(R.id.newNotification).visibility = View.GONE
        }
    }

    override fun onDestroy(){

        super.onDestroy()
        unregisterReceiver(connectivityChange)

        if(mSocket.connected()){
            mSocket.disconnect()
        }
    }

    private fun isIntentAvailable(ctx: Context, intent: Intent): Boolean {

        val packageManager = ctx.packageManager
        val list = packageManager.queryIntentActivities(intent,  PackageManager.MATCH_DEFAULT_ONLY)

        return list.size > 0
    }

    private val connectivityChange: BroadcastReceiver = object : BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?) {

            if(intent?.extras != null){

                when(intent.getBooleanExtra("connected", false)){
                    true -> {

                        loadUiComponents()
                        connectToServer()

                        Handler().postDelayed({
                            if(!mSocket.connected()){
                                showOfflineMessage()
                            }
                        }, 15000)
                    }
                    false -> {

                        showOfflineMessage()
                    }
                }


            }

        }
    }

    private fun showNotification(title: String, text: String, intent: Intent){

       val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val remoteView = RemoteViews(packageName, R.layout.push_notification)
        val remoteViewExpanded = RemoteViews(packageName, R.layout.push_notification_expanded)

        remoteView.setTextViewText(R.id.pushTitle, title)
        remoteView.setTextViewText(R.id.pushMessage, text)

        remoteViewExpanded.setTextViewText(R.id.pushTitle, title)
        remoteViewExpanded.setTextViewText(R.id.pushMessage, text)

        val builder = NotificationCompat.Builder(this, getString(R.string.app_name)).let {

            it.setSmallIcon(R.drawable.vlog)
            it.setCustomContentView(remoteView)
            it.priority = NotificationCompat.PRIORITY_MAX
            it.setAutoCancel(true)
            it.setContentIntent(pendingIntent)
        }

        with(NotificationManagerCompat.from(this)){

            notify(0, builder.build())
        }
    }

    private fun loadUiComponents(){

        if(!articlesLoaded){

            refreshLayout.visibility = View.VISIBLE

            doAsync {

                val jsonString: String = URL("https://vlog-io.herokuapp.com/news").readText()

                uiThread {

                    if(jsonString.isNotEmpty()){

                        val newsList = gson.fromJson(jsonString, VlogArticleContainer::class.java)

                        databaseHelper.clearAll(2)
                        val itemCount =  newsRecyclerAdapter.items.size
                        newsRecyclerAdapter.items.clear()
                        newsRecyclerAdapter.notifyItemRangeRemoved(0, itemCount)

                        newsList.results.forEach {

                            val dateInLocal = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(it.publishDate)
                            val hrs = ((((Date().time - dateInLocal!!.time)/1000)/60)/60)
                            val days = hrs/24

                            if(days < 1){

                                databaseHelper.cachedArticles = mutableListOf(it)
                                newsRecyclerAdapter.addList(mutableListOf(it))

                            }

                        }

                        newsRecyclerAdapter.updateItemRange()

                        Handler().postDelayed({
                            if(!nativeAdLoader.isLoading){

                                when {

                                    adsLoaded < 5 -> {
                                        nativeAdLoader.loadAds(AdRequest.Builder().build(), 5)
                                    }
                                }
                            }

                        }, 2000)

                        articlesLoaded = true
                        newsRecycler.layoutManager?.scrollToPosition(0)


                    }else{
                        refreshLayout.visibility = View.GONE
                    }
                }

            }

        }

    }

    private fun connectToServer(){
        mSocket.let {
            it.connect()
                .on(Socket.EVENT_CONNECT){
                    runOnUiThread {
                        findViewById<AppCompatTextView>(R.id.mainActivityToggle).visibility = View.GONE

                        if(pendingLogin != null){
                            login(pendingLogin.toString())
                            pendingLogin = null
                        }

                        getSupportAccount()

                    }
                }
                .on(Socket.EVENT_DISCONNECT){
                    runOnUiThread {
                        refreshLayout.visibility = View.GONE
                        showOfflineMessage()
                    }
                }
        }
    }

    private fun getSupportAccount(){

        mSocket.emit("getSupportAccount", "account", Ack {
            runOnUiThread {

                val supportAccount = it[0].toString()

                uri = Uri.parse(supportAccount)

            }
        })
    }

    private fun login(userPacket: String){

        mSocket.emit("login", userPacket, Ack {
            runOnUiThread {

                val serverPacket = it[0].toString()

                if(serverPacket.isNotEmpty()){

                    val packetToUser = gson.fromJson(serverPacket, User::class.java)
                    databaseHelper.user = packetToUser

                    contentLayoutToolbarImage.text = packetToUser.username[0].toString()

                }

            }
        })
    }

    private fun reviewApp(){

        val reviewManager = ReviewManagerFactory.create(this)

        val request = reviewManager.requestReviewFlow()

        request.addOnCompleteListener {

            if(it.isSuccessful){

                val reviewInfo = it.result

                reviewManager.launchReviewFlow(this@MainActivity, reviewInfo)
                    .addOnCompleteListener {

                        reviewCompleted()
                    }

            }else {

                Toast.makeText(this, "can't process review at the moment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reviewCompleted(){
        databaseHelper.canRead = "#"
        CustomDialogs.hint(this, "thanks alot, we'll buy you a donut some other time : )")
    }

    private fun reviewLater(){

        val dateFromLast = Date()
        databaseHelper.canRead = "$dateFromLast"
    }

    private fun neverReview(){
        databaseHelper.canRead = "#"
        CustomDialogs.hint(this, "we won't show you this again")
    }

    private fun showOfflineMessage(){

        refreshLayout.visibility = View.GONE
        mainActivityToggle.visibility = View.VISIBLE
        mainActivityToggle.text = resources.getString(R.string.YOU_ARE_OFFLINE)
    }

    private fun connectionListener(){

        // configuring device online worker

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val builder = JobInfo.Builder(NETWORK_STATUS_ID, ComponentName(packageName, VlogEventEmmitter().javaClass.name))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)

        if(jobScheduler.schedule(builder.build()) <= 0){
            finishAffinity()
        }

    }

    companion object {

        private const val LOGIN_REQUEST_CODE = 0
        private const val READ_REQUEST_CODE = 1
        private const val SAVED_REQUEST_CODE = 2
        private const val NETWORK_STATUS_ID = 0
        private const val BROADCAST_ACTION = "com.ace.vlog"

    }

}
