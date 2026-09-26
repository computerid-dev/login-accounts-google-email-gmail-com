package com.gad.production.by.nyr

import android.app.*
import android.os.Bundle
import android.content.*
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.print.*
import android.view.*
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*

class MainActivity: Activity(){
 private val p by lazy{getSharedPreferences("gad",0)}
 private val nf=NumberFormat.getCurrencyInstance(Locale("id","ID"))
 private lateinit var date:EditText; private lateinit var loc:EditText; private lateinit var veh:EditText; private lateinit var plate:EditText
 private lateinit var op:EditText; private lateinit var fuel:EditText; private lateinit var toll:EditText; private lateinit var park:EditText; private lateinit var sec:EditText; private lateinit var meal:EditText
 private lateinit var total:TextView; private lateinit var remain:TextView
 private var editing=-1

 override fun onCreate(b:Bundle?){super.onCreate(b); ui()}

 private fun ui(){
  val s=ScrollView(this); val l=LinearLayout(this); l.orientation=LinearLayout.VERTICAL; l.setPadding(24,20,24,24)
  val h=TextView(this); h.text="GAD OPERASIONAL"; h.textSize=28f; h.setTypeface(null,1); l.addView(h)
  val sub=TextView(this); sub.text="LAPORAN OPERASIONAL KENDARAAN"; sub.textSize=15f; l.addView(sub)
  date=f("Tanggal Event"); loc=f("Lokasi Event"); veh=f("Jenis Kendaraan"); plate=f("Nomor Polisi")
  op=f("Uang Operasional"); fuel=f("Solar / Bensin"); toll=f("Tol"); park=f("Parkir"); sec=f("Uang Security"); meal=f("Uang Makan")
  listOf(date,loc,veh,plate,op,fuel,toll,park,sec,meal).forEach{l.addView(it)}
  total=tv("Total Pengeluaran: Rp 0",18); remain=tv("Sisa Uang: Rp 0",21); l.addView(total); l.addView(remain)
  val hit=btn("HITUNG OTOMATIS"); val save=btn("SIMPAN LAPORAN"); val hist=btn("RIWAYAT LAPORAN"); val pr=btn("PRINT / PDF")
  listOf(hit,save,hist,pr).forEach{l.addView(it)}
  hit.setOnClickListener{calc()}; save.setOnClickListener{save()}; hist.setOnClickListener{history()}; pr.setOnClickListener{print()}
  s.addView(l); setContentView(s)
 }
 private fun f(h:String)=EditText(this).apply{hint=h; setPadding(8,8,8,8)}
 private fun tv(t:String,z:Float)=TextView(this).apply{text=t;textSize=z;setPadding(0,14,0,10)}
 private fun btn(t:String)=Button(this).apply{text=t}
 private fun num(e:EditText)=e.text.toString().replace("[^0-9]".toRegex(),"").toLongOrNull()?:0
 private fun calc():Long{val x=num(fuel)+num(toll)+num(park)+num(sec)+num(meal); total.text="Total Pengeluaran: ${nf.format(x)}";remain.text="Sisa Uang: ${nf.format(num(op)-x)}";return x}
 private fun save(){
  val a=JSONArray(p.getString("reports","[]")); val o=JSONObject()
  o.put("date",date.text);o.put("loc",loc.text);o.put("veh",veh.text);o.put("plate",plate.text);o.put("op",num(op));o.put("fuel",num(fuel));o.put("toll",num(toll));o.put("park",num(park));o.put("sec",num(sec));o.put("meal",num(meal));o.put("total",calc());o.put("remain",num(op)-o.getLong("total"))
  if(editing>=0){a.put(editing,o);editing=-1}else a.put(o);p.edit().putString("reports",a.toString()).apply()
  Toast.makeText(this,"Laporan tersimpan",Toast.LENGTH_SHORT).show()
 }
 private fun history(){
  val a=JSONArray(p.getString("reports","[]")); val items=ArrayList<String>()
  for(i in 0 until a.length()){val o=a.getJSONObject(i);items.add("${i+1}. ${o.getString("date")} | ${o.getString("loc")}\nSisa: ${nf.format(o.getLong("remain"))}")}
  if(items.isEmpty()){AlertDialog.Builder(this).setTitle("Riwayat").setMessage("Belum ada laporan.").setPositiveButton("OK",null).show();return}
  AlertDialog.Builder(this).setTitle("Riwayat Laporan").setItems(items.toTypedArray()){_,which->
   val o=a.getJSONObject(which); AlertDialog.Builder(this).setTitle("Laporan ${which+1}").setMessage(
    "Tanggal: ${o.getString("date")}\nLokasi: ${o.getString("loc")}\nKendaraan: ${o.getString("veh")}\nNo Polisi: ${o.getString("plate")}\n\nOperasional: ${nf.format(o.getLong("op"))}\nSolar/Bensin: ${nf.format(o.getLong("fuel"))}\nTol: ${nf.format(o.getLong("toll"))}\nParkir: ${nf.format(o.getLong("park"))}\nSecurity: ${nf.format(o.getLong("sec"))}\nMakan: ${nf.format(o.getLong("meal"))}\n\nTotal: ${nf.format(o.getLong("total"))}\nSisa: ${nf.format(o.getLong("remain"))}"
   ).setNegativeButton("Hapus"){_,_->a.remove(which);p.edit().putString("reports",a.toString()).apply()}
    .setNeutralButton("Edit"){_,_->load(o,which)}.setPositiveButton("Tutup",null).show()
  }.show()
 }
 private fun load(o:JSONObject,i:Int){editing=i;date.setText(o.getString("date"));loc.setText(o.getString("loc"));veh.setText(o.getString("veh"));plate.setText(o.getString("plate"));op.setText(o.getLong("op").toString());fuel.setText(o.getLong("fuel").toString());toll.setText(o.getLong("toll").toString());park.setText(o.getLong("park").toString());sec.setText(o.getLong("sec").toString());meal.setText(o.getLong("meal").toString());calc()}
 private fun print(){
  val text="""GAD OPERASIONAL
LAPORAN OPERASIONAL KENDARAAN

Tanggal Event : ${date.text}
Lokasi Event  : ${loc.text}
Kendaraan     : ${veh.text}
No. Polisi    : ${plate.text}

Uang Operasional : ${nf.format(num(op))}
Solar / Bensin   : ${nf.format(num(fuel))}
Tol              : ${nf.format(num(toll))}
Parkir            : ${nf.format(num(park))}
Security          : ${nf.format(num(sec))}
Makan             : ${nf.format(num(meal))}

Total Pengeluaran: ${nf.format(calc())}
Sisa Uang        : ${nf.format(num(op)-calc())}
"""
  val pm=getSystemService(PRINT_SERVICE) as PrintManager
  pm.print("GAD_OPERASIONAL",object:PrintDocumentAdapter(){
   var pdf:PdfDocument?=null
   override fun onLayout(o:PrintAttributes?,n:PrintAttributes?,c:android.os.CancellationSignal?,cb:LayoutResultCallback?,e:Bundle?){pdf=PdfDocument();cb?.onLayoutFinished(PrintDocumentInfo.Builder("GAD_OPERASIONAL.pdf").setPageCount(1).build(),true)}
   override fun onWrite(r:Array<PrintDocumentInfo.PageRange>,d:android.os.ParcelFileDescriptor?,c:android.os.CancellationSignal?,cb:WriteResultCallback?){
    val pg=pdf!!.startPage(PdfDocument.PageInfo.Builder(595,842,1).create());val paint=Paint();paint.textSize=13f
    var y=55f;text.lines().forEach{pg.canvas.drawText(it,40f,y,paint);y+=20};pdf!!.finishPage(pg);pdf!!.writeTo(java.io.FileOutputStream(d!!.fileDescriptor));pdf!!.close();cb?.onWriteFinished(arrayOf(PrintDocumentInfo.PageRange.ALL_PAGES))
   }
  },null)
 }
}