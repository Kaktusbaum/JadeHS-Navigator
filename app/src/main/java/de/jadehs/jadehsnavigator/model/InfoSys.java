/**
 * This file is part of JadeHS-Navigator.
 *
 * JadeHS-Navigator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JadeHS-Navigator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JadeHS-Navigator.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jadehs.jadehsnavigator.model;

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.jadehs.jadehsnavigator.database.InfoSysItemDataSource;
import de.jadehs.jadehsnavigator.util.CalendarHelper;

public class InfoSys {

    //private int lastUpdate;
    private Context context;
    private String lastUpdate;
    private ArrayList<InfoSysItem> infoSysItems = null;
    private CalendarHelper calendarHelper;
    private  Date date;

    private String url;
    private int fb;

    public InfoSys(Context context, String url, int fb){
        this.context = context;
        this.url = url;
        this.fb = fb;
        this.calendarHelper= new CalendarHelper();
    }

    public ArrayList<InfoSysItem> parse(){
        /*
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy h:mm:ss");
        String formattedDate = sdf.format(date);
        */

        //SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        //SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy   h:mm:ss", Locale.US);

        //String formattedDate = sdf.format(date);

        this.lastUpdate = calendarHelper.getDateRightNow(true);
        this.infoSysItems = new ArrayList<InfoSysItem>();

        try {
            InfoSysItemDataSource infoSysItemDataSource = new InfoSysItemDataSource(this.context);
            infoSysItemDataSource.open();

            InfoSysItem infoSysItem;

            Document doc = Jsoup.connect(this.url)
                    .parser(Parser.xmlParser())
                    .get();
            Log.i("PARSING TAG", "Done Parsing");

            for (Element item : doc.select("item")) {
                String title = item.select("title").first().text();
                String description = item.select("description").first().text();
                //String link = item.select("link").first().nextSibling().toString();
                String link = item.select("link").first().text();
                String detailDescription = "";
                try {
                    Document detailDoc = Jsoup.connect(link).parser(Parser.htmlParser()).get();
                    detailDescription = detailDoc.body().getElementsByClass("newstext").get(1).html();
                }catch (Exception ex){
                    Log.wtf("LinkEx", "Failed", ex);
                }
                String fullDescription = description + detailDescription;

                String creator = item.select("dc|creator").first().text();
                String created = item.select("dc|date").first().text();

                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
                String dateStr = "";
                try {
                    date = sdf2.parse(created);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    // Set date string (and btw Calendar.MONTH returns 0-11 for some reason)
                    dateStr = cal.get(Calendar.DAY_OF_MONTH) + "." + (cal.get(Calendar.MONTH)+1) + "." + cal.get(Calendar.YEAR) + "   " +
                            cal.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", cal.get(Calendar.MINUTE)) + " Uhr";
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                infoSysItem = new InfoSysItem(title,fullDescription,link,creator,dateStr,this.fb);
                infoSysItemDataSource.createInfoSysItem(infoSysItem);

                this.infoSysItems.add(infoSysItem);

                Log.i("ITEM TAG", "ADDED ITEM");
            }
            infoSysItemDataSource.close();
        }catch (IOException e){
            Log.e("Parse", "Parsing failed");
            e.printStackTrace();
        }catch (SQLException e){
            Log.wtf("ERROR", e.getMessage());
            e.printStackTrace();
        }
        Log.i("LIST TAG", "FINISHED ADDING ITEMS");
        return infoSysItems;
    }

    public List<InfoSysItem> getInfoSysItems(){
        return this.infoSysItems;
    }

    public String getLastUpdate(){
        return this.lastUpdate;
    }
}
