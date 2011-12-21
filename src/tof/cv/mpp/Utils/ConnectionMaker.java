package tof.cv.mpp.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import tof.cv.mpp.R;
import tof.cv.mpp.bo.Connections;
import tof.cv.mpp.bo.Station;
import tof.cv.mpp.bo.Train;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;

/*
 * 
 * THIS IS GARBAGE AND WILL BE CLEANED/DELETED SOON!!!!!!
 * 
 */

public class ConnectionMaker {

	private static ConnectionDbAdapter mDbHelper;
	final static String DIRPATH = "/Android/data/BeTrains";
	final static String FILENAMECONN = "connections.txt";

	public final static String[] LIST_OF_EURO_STATIONS = new String[] {
			"FR/LILLE", "FR/PARIS", "FR/STRASBOURG", "DE/AACHEN HBF",
			"DE/KOLN HBF", "LU/LUXEMBOURG", "NL/DEN HAAG", "NL/AMSTERDAM",
			"NL/MAASTRICHT", "NL/ROTTERDAM" };

	public final static String[] LIST_OF_FAV_STATIONS = new String[] { "FAV1",
			"FAV2", "FAV3", "FAV4", "FAV5" };

	public final static String[] LIST_OF_STATIONS = new String[] {
			"'S GRAVENBRAKEL", "AALST", "AALST KERREBROEK", "AALTER", "AARLEN",
			"AARSCHOT", "AARSELE", "AAT", "ACREN", "AISEAU", "ALKEN", "ALOST",
			"AMAY", "AMPSIN", "ANDENNE", "ANGLEUR", "ANS", "ANSEREMME",
			"ANTOING", "ANTWERPEN BERCHEM", "ANTWERPEN CENTRAAL",
			"ANTWERPEN DAM", "ANTWERPEN HAVEN", "ANTWERPEN LUCHTBAL",
			"ANTWERPEN NOORDERDOKKEN", "ANTWERPEN OOST", "ANTWERPEN ZUID",
			"ANVERS BERCHEM", "ANVERS CENTRAL", "ANVERS DAM", "ANVERS EST",
			"ANVERS HAVEN", "ANVERS LUCHTBAL", "ANVERS NOORDERDOKKEN",
			"ANVERS SUD", "ANZEGEM", "APPELTERRE", "ARCHENNES", "ARLON",
			"ASSE", "ASSESSE", "ATH", "ATHUS", "AUBANGE", "AUDENARDE",
			"AUVELAIS", "AYE", "AYWAILLE", "BAASRODE SUD", "BAASRODE ZUID",
			"BALEGEM DORP", "BALEGEM SUD", "BALEGEM VILLAGE", "BALEGEM ZUID",
			"BALEN", "BAMBRUGGE", "BARVAUX", "BAS OHA", "BASSE WAVRE",
			"BASTENAKEN NOORD", "BASTENAKEN ZUID", "BASTOGNE NORD",
			"BASTOGNE SUD", "BEAURAING", "BEERNEM", "BEERSEL", "BEERVELDE",
			"BEGIJNENDIJK", "BEIGNEE", "BELLEM", "BELSELE",
			"BERCHEM SAINTE AGATHE", "BERGEN", "BERINGEN", "BERLAAR",
			"BERTRIX", "BERZEE", "BEUZET", "BEVEREN(WAAS)", "BEVEREN(WAES)",
			"BIERGES WALIBI", "BIERSET AWANS", "BILZEN", "BINCHE", "BISSEGEM",
			"BLANKENBERGE", "BLANMONT", "BLATON", "BLERET", "BOCKSTAEL",
			"BOECHOUT", "BOITSFORT", "BOKRIJK", "BOMAL", "BOOISCHOT", "BOOM",
			"BOONDAAL", "BOONDAEL", "BOORTMEERBEEK", "BORDET", "BORGWORM",
			"BORNEM", "BOSVOORDE", "BOURG LEOPOLD", "BOUSSU", "BOUWEL",
			"BRACQUEGNIES", "BRAINE L'ALLEUD", "BRAINE LE COMTE", "BRESSOUX",
			"BRUGELETTE", "BRUGES", "BRUGES SAINT PIERRE", "BRUGGE",
			"BRUGGE SINT PIETERS", "BRUSSEL CENTRAAL", "BRUSSEL CONGRES",
			"BRUSSEL KAPELLEKERK", "BRUSSEL LUXEMBURG",
			"BRUSSEL NAT LUCHTHAVEN", "BRUSSEL NOORD", "BRUSSEL SCHUMAN",
			"BRUSSEL ZUID", "BRUXELLES CENTRAL", "BRUXELLES CHAPELLE",
			"BRUXELLES CONGRES", "BRUXELLES LUXEMBOURG", "BRUXELLES MIDI",
			"BRUXELLES NAT AEROPORT", "BRUXELLES NORD", "BRUXELLES SCHUMAN",
			"BUDA", "BUGGENHOUT", "BUIZINGEN", "BURST", "CALLENELLE",
			"CAMBRON CASTEAU", "CARLSBOURG", "CARNIERES", "CEROUX MOUSTY",
			"CHAPELLE DIEU", "CHAPOIS", "CHARLEROI OUEST", "CHARLEROI SUD",
			"CHARLEROI WEST", "CHARLEROI ZUID", "CHASTRE",
			"CHATEAU DE SEILLES", "CHATELET", "CHENEE", "CINEY",
			"COMBLAIN LA TOUR", "COMINES", "COO", "COUILLET",
			"COURCELLES MOTTE", "COURRIERE", "COUR SUR HEURE", "COURTRAI",
			"COURT SAINT ETIENNE", "COUVIN", "DAVE SAINT MARTIN", "DE HOEK",
			"DE PANNE", "DE PINTE", "DEINZE", "DELTA", "DENDERLEEUW",
			"DENDERMONDE", "DIEGEM", "DIEPENBEEK", "DIESDELLE", "DIEST",
			"DIKSMUIDE", "DILBEEK", "DINANT", "DIXMUDE", "DOLHAIN GILEPPE",
			"DOORNIK", "DRONGEN", "DUFFEL", "DUINBERGEN", "ECAUSSINNES", "EDE",
			"EDINGEN", "EEKLO", "EERKEN", "EICHEM", "EIGENBRAKEL", "EINE",
			"EKE NAZARETH", "EKEREN", "ENGHIEN", "ENGIS", "EPPEGEM",
			"ERBISOEUL", "EREMBODEGEM", "ERNAGE", "ERPE MERE", "ERPS KWERPS",
			"ERQUELINNES", "ERQUELINNES DORP", "ERQUELINNES VILLAGE", "ESNEUX",
			"ESSEN", "ESSENE LOMBEEK", "ETTERBEEK", "EUPEN", "EVERE",
			"EVERGEM", "EZEMAAL", "FAMILLEUREUX", "FARCIENNES", "FAUX",
			"FEXHE LE HAUT CLOCHER", "FLAWINNE", "FLEMALLE GRANDE",
			"FLEMALLE HAUTE", "FLEURUS", "FLOREE", "FLOREFFE", "FLORENVILLE",
			"FLORIVAL", "FONTAINE VALMONT", "FORCHIES", "FOREST EST",
			"FOREST MIDI", "FORRIERES", "FRAIPONT", "FRAMERIES", "FRANCHIMONT",
			"FRANIERE", "FROYENNES", "FURNES", "GALMAARDEN", "GAMMERAGES",
			"GAND DAMPOORT", "GAND SAINT PIERRE", "GASTUCHE", "GAVERE ASPER",
			"GEDINNE", "GEEL", "GEMBLOUX", "GENDRON CELLES", "GENK", "GENLY",
			"GENTBRUGGE", "GENT DAMPOORT", "GENT SINT PIETERS", "GENVAL",
			"GERAARDSBERGEN", "GHLIN", "GLAAIEN", "GLONS", "GODARVILLE",
			"GODINNE", "GONTRODE", "GOUVY", "GOUY LEZ PIETON", "GRAIDE",
			"GRAMMONT", "GROENENDAAL", "GROOT BIJGAARDEN", "GRUPONT", "HAACHT",
			"HAALTERT", "HABAY", "HAININ", "HAL", "HALANZY", "HALLE", "HAMBOS",
			"HAMOIR", "HAM SUR HEURE", "HAM SUR SAMBRE", "HANSBEKE",
			"HARCHIES", "HARELBEKE", "HAREN", "HAREN SUD", "HAREN ZUID",
			"HASSELT", "HAUTE FLâNE", "HAVERSIN", "HAVRE", "HEIDE", "HEIST",
			"HEIST OP DEN BERG", "HEIZIJDE", "HEMIKSEM", "HENNUYERES",
			"HERENT", "HERENTALS", "HERGENRATH", "HERNE", "HERSEAUX",
			"HERSTAL", "HERZELE", "HEUSDEN", "HEVER", "HEVERLEE", "HILLEGEM",
			"HOBOKEN POLDER", "HOEI", "HOEILAART", "HOFSTADE", "HOLLEKEN",
			"HONY", "HOURAING", "HOURPES", "HOUYET", "HOVE", "HUIZINGEN",
			"HUY", "IDDERGEM", "IDEGEM", "IEPER", "INGELMUNSTER", "IZEGEM",
			"JAMBES", "JAMBES EST", "JAMBES OOST", "JAMIOULX", "JEMAPPES",
			"JEMELLE", "JEMEPPE SUR MEUSE", "JEMEPPE SUR SAMBRE", "JETTE",
			"JURBEKE", "JURBISE", "JUSLENVILLE", "KALMTHOUT", "KAPELLEN",
			"KAPELLE OP DEN BOS", "KESSEL", "KIEWIT", "KIJKUIT", "KNOKKE",
			"KOKSIJDE", "KOMEN", "KONTICH", "KORTEMARK", "KORTENBERG",
			"KORTRIJK", "KWATRECHT", "LA HULPE", "LA LOUVIERE CENTRE",
			"LA LOUVIERE CENTRUM", "LA LOUVIERE SUD", "LA LOUVIERE ZUID",
			"LA PANNE", "LA ROCHE(BRABANT)", "LABUISSIERE", "LANDEGEM",
			"LANDELIES", "LANDEN", "LANDSKOUTER", "LANGDORP", "LE CAMPINAIRE",
			"LEBBEKE", "LEDE", "LEIGNON", "LEMAN", "LEMBEEK", "LENS",
			"LEOPOLDSBURG", "LESSEN", "LESSINES", "LEUVEN", "LEUZE", "LEVAL",
			"LIBRAMONT", "LICHTERVELDE", "LIEDEKERKE", "LIEGE GUILLEMINS",
			"LIEGE JONFOSSE", "LIEGE PALAIS", "LIER", "LIERDE", "LIERRE",
			"LIERS", "LIGNY", "LILLOIS", "LIMAL", "LINKEBEEK", "LISSEWEGE",
			"LOBBES", "LODELINSART", "LOKEREN", "LOMMEL", "LONDERZEEL",
			"LONZEE", "LOT", "LOUVAIN", "LOUVAIN LA NEUVE UNIVERSITE",
			"LOUVAIN LA NEUVE UNIVERSITEIT", "LUIK GUILLEMINS",
			"LUIK JONFOSSE", "LUIK PALEIS", "LUSTIN", "LUTTRE", "MAFFLE",
			"MALDEREN", "MALINES", "MALINES NEKKERSPOEL", "MANAGE", "MARBEHAN",
			"MARCHE EN FAMENNE", "MARCHE LES DAMES", "MARCHE LEZ ECAUSSINNES",
			"MARCHIENNE AU PONT", "MARCHIENNE ZONE", "MARIA AALTER",
			"MARIEMBOURG", "MARLOIE", "MASNUY SAINT PIERRE", "MAUBRAY", "MAZY",
			"MECHELEN", "MECHELEN NEKKERSPOEL", "MEISER", "MELKOUWEN", "MELLE",
			"MELREUX HOTTON", "MELSELE", "MENEN", "MENIN", "MERCHTEM",
			"MERELBEKE", "MERODE", "MERY", "MESSANCY", "MEVERGNIES ATTRE",
			"MILMORT", "MOENSBERG", "MOESKROEN", "MOL", "MOLLEM", "MOMALLE",
			"MONS", "MONT SAINT GUIBERT", "MOORTSELE", "MORLANWELZ", "MORTSEL",
			"MORTSEL DEURNESTEENWEG", "MORTSEL LIERSESTEENWEG",
			"MORTSEL OUDE GOD", "MOUSCRON", "MOUSTIER", "MUIZEN", "MUNKZWALM",
			"NAMECHE", "NAMEN", "NAMUR", "NANINNE", "NATOYE", "NEERPELT",
			"NEERWINDEN", "NESSONVAUX", "NEUFCHATEAU", "NEUFVILLES", "NIEL",
			"NIEUWKERKEN WAAS", "NIJLEN", "NIJVEL", "NIMY", "NINOVE",
			"NIVELLES", "NOORDERKEMPEN", "NOSSEGEM", "OBAIX BUZET", "OBOURG",
			"OKEGEM", "OLEN", "OOSTENDE", "OOSTKAMP", "OPWIJK", "OPZULLIK",
			"OSTENDE", "OTTIGNIES", "OUDEGEM", "OUDENAARDE", "OUD HEVERLEE",
			"OVERPELT", "PALISEUL", "PAPEGEM", "PAPIGNIES", "PECROT",
			"PEPINSTER", "PEPINSTER CITE", "PERUWELZ", "PHILIPPEVILLE",
			"PIETON", "POIX SAINT HUBERT", "PONT · CELLES", "PONT DE SERAING",
			"POPERINGE", "POULSEUR", "PROFONDSART", "PRY", "PUURS",
			"QUAREGNON", "QUEVY", "QUIEVRAIN", "REBAIX", "REMICOURT", "RENAIX",
			"RHISNES", "RHODE SAINT GENESE", "RIVAGE", "RIXENSART",
			"ROESELARE", "RONET", "RONSE", "ROULERS", "ROUX", "RUISBROEK",
			"RUISBROEK SAUVEGARDE", "SAINT DENIS BOVESSE", "SAINT GHISLAIN",
			"SAINT JOB", "SAINT NICOLAS", "SAINT TROND", "SART BERNARD",
			"SCHAARBEEK", "SCHAERBEEK", "SCHELDEWINDEKE", "SCHELLE",
			"SCHELLEBELLE", "SCHENDELBEKE", "SCHOONAARDE", "SCHULEN",
			"SCLAIGNEAUX", "SCLESSIN", "SERSKAMP", "'S GRAVENBRAKEL", "SILLY",
			"SINAAI", "SINT AGATHA BERCHEM", "SINT DENIJS BOEKEL",
			"SINT GENESIUS RODE", "SINT GILLIS", "SINT GILLIS(TERMONDE)",
			"SINT JOB", "SINT JORIS WEERT", "SINT KATELIJNE WAVER",
			"SINT MARIABURG", "SINT MARTENS BODEGEM", "SINT NIKLAAS",
			"SINT TRUIDEN", "SLEIDINGE", "SOIGNIES", "SOLRE SUR SAMBRE", "SPA",
			"SPA GERONSTERE", "STATTE", "STOCKEM", "SY", "TAMINES", "TAMISE",
			"TEMSE", "TERHAGEN", "TERHULPEN", "TERMONDE", "TERNAT", "TESTELT",
			"THEUX", "THIEU", "THUIN", "THULIN", "TIELEN", "TIELT", "TIENEN",
			"TILFF", "TILLY", "TIRLEMONT", "TOLLEMBEEK", "TONGEREN", "TONGRES",
			"TORHOUT", "TOURNAI", "TROIS PONTS", "TRONCHIENNES", "TROOZ",
			"TUBEKE", "TUBIZE", "TURNHOUT", "UCCLE CALEVOET", "UCCLE STALLE",
			"UKKEL KALEVOET", "UKKEL STALLE", "VELTEM", "VERTRIJK",
			"VERVIERS CENTRAAL", "VERVIERS CENTRAL", "VERVIERS PALAIS",
			"VERVIERS PALEIS", "VEURNE", "VIANE MOERBEKE", "VICHTE",
			"VIELSALM", "VIJFHUIZEN", "VILLE POMMEROEUL", "VILLERS LA VILLE",
			"VILVOORDE", "VILVORDE", "VIRTON", "VISE", "VIVIER D'OIE",
			"VIVILLE", "VOROUX", "VORST OOST", "VORST ZUID", "WAARSCHOOT",
			"WALCOURT", "WAREGEM", "WAREMME", "WATERLOO", "WATERMAAL",
			"WATERMAEL", "WAVER", "WAVRE", "WEERDE", "WELKENRAEDT", "WELLE",
			"WERVIK", "WESPELAAR TILDONK", "WETTEREN", "WEVELGEM", "WEZEMAAL",
			"WEZET", "WICHELEN", "WIJGMAAL", "WILDERT", "WILLEBROEK",
			"WOLFSTEE", "WONDELGEM", "YPRES", "YVES GOMEZEE", "YVOIR",
			"ZANDBERGEN", "ZAVENTEM", "ZEDELGEM", "ZEEBRUGGE DORP",
			"ZEEBRUGGE STRAND", "ZELE", "ZELLIK", "ZICHEM", "ZINGEM", "ZINNIK",
			"ZOLDER", "ZOTTEGEM", "ZWANKENDAMME", "ZWIJNDRECHT" };

	/*
	 * public static void fillDate(Activity context, String pYear, String
	 * pMonth, String pDay) { final TextView textYear = (TextView)
	 * context.findViewById(R.id.tv_year); final TextView textMonth = (TextView)
	 * context .findViewById(R.id.tv_month); final TextView textDay = (TextView)
	 * context.findViewById(R.id.tv_day); textYear.setText(fillZero(pYear));
	 * textMonth.setText(fillZero(pMonth)); textDay.setText(fillZero(pDay)); }
	 * 
	 * public static void fillTime(Activity context, String pHour, String
	 * pMinute) { final TextView textHour = (TextView)
	 * context.findViewById(R.id.tv_hour); final TextView textMinute =
	 * (TextView) context .findViewById(R.id.tv_minut);
	 * textHour.setText(fillZero(pHour)); textMinute.setText(fillZero(pMinute));
	 * }
	 */

	public static void createAlertDialog(String title, String body,
			Context context) {

		AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);
		alt_bld.setMessage(body)
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Action for 'Yes' Button
							}
						});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(title);
		alert.show();
	}

	public static void createAlertDialogAndFinish(String title, String body,
			final Context context) {

		AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);
		alt_bld.setMessage(body)
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Action for 'Yes' Button
								((Activity) context).finish();
							}
						});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(title);
		alert.show();
	}

	public static void addAsStarred(String item, String item2, int type,
			Context context) {
		mDbHelper = new ConnectionDbAdapter(context);
		mDbHelper.open();
		mDbHelper.createFav(item, item2, type);
		mDbHelper.close();

	}

	public static Connections getAPIConnections(String year, String month,
			String day, String hour, String minutes, String language,
			String departure, String arrival, String departureArrival,
			String trainsOnly, final Context context) {
		String TAG = "BETRAINS";

		mDbHelper = new ConnectionDbAdapter(context);
		mDbHelper.open();

		if (day.length() == 1)
			day = "0" + day;

		if (month.length() == 1)
			month = "0" + month;
		if (month.contentEquals("13"))
			month = "01";

		String url = "http://api.irail.be/connections.php?to="
				// String url = "http://dev.api.irail.be/connections.php?to="
				+ arrival + "&from=" + departure + "&date=" + day + month
				+ year + "&time=" + hour + minutes + "&timeSel="
				+ departureArrival + "&lang=" + language + "&typeOfTransport="
				+ trainsOnly + "&format=json&fast=true";
		url = url.replace(" ", "%20");
		Log.v(TAG, url);

		try {
			InputStream is = Utils.DownloadJsonFromUrlAndCacheToSd(url,
					DIRPATH, FILENAMECONN, context);

			Gson gson = new Gson();
			final Reader reader = new InputStreamReader(is);
			return gson.fromJson(reader, Connections.class);

		} catch (Exception ex) {
			ex.printStackTrace();
			Log.i("", "*******");
			mDbHelper.close();
			return null;
		}
	}

	public static Vehicle getAPIvehicle(String year, String month, String day,
			String hour, String minutes, String language, String vehicle,
			final Context context) {
		String TAG = "getAPITrain";

		String langue = context.getString(R.string.url_lang_2);
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				"prefnl", false))
			langue = "nl";

		String url = "http://api.irail.be/vehicle.php/?id=" + vehicle
				+  "&format=JSON&fast=true";
		//url+="&lang=" + langue ;
		System.out.println("Affiche les infos train depuis la page: " + url);

		try {
			// Log.i(TAG, "Json Parser started..");
			Gson gson = new Gson();
			Reader r = new InputStreamReader(getJSONData(url, context));
			return gson.fromJson(r, Vehicle.class);

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	public class Vehicle {

		private VehicleStops stops;
		private String version;

		public VehicleStops getVehicleStops() {
			return stops;
		}

		public String getVersion() {
			return version;
		}

	}
	
	public class VehicleStops {

		private List<VehicleStop> stop;

		public List<VehicleStop> getVehicleStop() {
			return stop;
		}
	}

	public class VehicleStop {

		private String station;
		private String time;
		private String delay;

		public String getStation() {
			return station;
		}
		public String getTime() {
			return time;
		}
		public String getDelay() {
			return delay;
		}
	}
	public static Connections getCachedConnections() {

		try {
			File memory = Environment.getExternalStorageDirectory();
			File dir = new File(memory.getAbsolutePath() + DIRPATH);
			dir.mkdirs();
			File file = new File(dir, FILENAMECONN);
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			Gson gson = new Gson();
			final Reader reader = new InputStreamReader(is);

			// TEST
			/*
			 * String chaine=""; BufferedReader br=new BufferedReader(reader);
			 * String ligne; while ((ligne=br.readLine())!=null){
			 * System.out.println(ligne); chaine+=ligne+"\n"; } br.close();
			 * Log.i("TAG", "***"+chaine);
			 */

			return gson.fromJson(reader, Connections.class);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String capitalize(String inputWord) {
		String firstLetter = inputWord.substring(0, 1); // Get first letter
		String remainder = inputWord.substring(1); // Get remainder of word.
		return firstLetter.toUpperCase() + remainder.toLowerCase();

	}

	public static String correctHTML(String bla)

	{
		return Html.fromHtml(bla).toString();
	}

	public class Liveboard {

		private Departures departures;
		private String station;
		private StationInfo stationinfo;

		public Departures getDepartures() {
			return departures;
		}

		public String getName() {
			return station;
		}

		public StationInfo getStationInfo() {
			return stationinfo;
		}

	}

	public class Departures {

		private List<Departure> departure;

		public List<Departure> getDepartures() {
			return departure;
		}

	}

	public class StationInfo {

		private String locationX;
		private String locationY;

		public String getLat() {
			return locationX;
		}

		public String getLon() {
			return locationY;
		}

	}

	public class Departure {
		public String vehicle;
		private String station;
		public String platform;
		private String time;
		private String delay;
		private double locationX;
		private double locationY;

		public Departure(String vehicle, String station, String delay,
				double locationX, double locationY) {
			this.vehicle = vehicle;
			this.station = station;
			this.delay = delay;
			this.locationX = locationX;
			this.locationY = locationY;
		}

		public String getName() {
			return this.station;
		}

		public String getVehicle() {
			return this.vehicle;
		}

		public String getPlatform() {
			return this.platform;
		}

		public String getTime() {
			return this.time;
		}

		public double getLat() {
			return this.locationX;
		}

		public double getLon() {
			return this.locationY;
		}

		public String getDelay() {
			return this.delay;
		}

	}

	public static InputStream getJSONData(String url, Context context) {
		DefaultHttpClient httpClient = new DefaultHttpClient();

		String myVersion = "0.0";
		PackageManager manager = context.getPackageManager();

		try {
			myVersion = (manager.getPackageInfo(context.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		httpClient.getParams().setParameter(
				"http.useragent",
				"BeTrains " + myVersion + " for Android - "
						+ System.getProperty("http.agent"));

		URI uri;
		InputStream data = null;
		try {
			uri = new URI(url);
			HttpGet method = new HttpGet(uri);
			HttpResponse response = httpClient.execute(method);
			data = response.getEntity().getContent();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

	public static ArrayList<Message> requestPhpRead(String trainId, int start,
			int span, Context context) {
		/*
		 * String TAG = "requestPhpRead"; ArrayList<Message> listOfMessages =
		 * new ArrayList<Message>(); // On cree le client HttpClient client =
		 * new HttpClient();
		 * 
		 * HttpClientParams clientParams = new HttpClientParams(); //
		 * clientParams.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, //
		 * "UTF-8"); client.setParams(clientParams);
		 * 
		 * PostMethod methode = new PostMethod(
		 * "http://christophe.frandroid.com/betrains/php/messages.php");
		 * methode.addParameter("code",
		 * "hZkzZDzsiF5354LP42SdsuzbgNBXZa78123475621857a");
		 * methode.addParameter("message_count", "" + span);
		 * methode.addParameter("message_index", "" + start);
		 * methode.addParameter("mode", "read"); methode.addParameter("order",
		 * "DESC"); Log.d(TAG, "tid is " + trainId); if (trainId != null)
		 * methode.addParameter("train_id", trainId);
		 * 
		 * // methode.addRequestHeader("Content-Type",
		 * "text/plain;charset=UTF-8"); // Le buffer qui nous servira a
		 * recuperer le code de la page BufferedReader br = null; String txt =
		 * ""; try { client.executeMethod(methode); br = new BufferedReader(new
		 * InputStreamReader(methode .getResponseBodyAsStream(),
		 * methode.getResponseCharSet())); String readLine; while (((readLine =
		 * br.readLine()) != null)) { System.out.println("readLine : " +
		 * readLine); txt += readLine; } } catch (Exception e) {
		 * e.printStackTrace(); } finally { methode.releaseConnection(); if (br
		 * != null) { try { br.close(); } catch (Exception e) {
		 * e.printStackTrace(); } } } if (!txt.equals("")) { String[] messages =
		 * txt.split("<message>");
		 * 
		 * int i = 1; if (messages.length > 1) {
		 * 
		 * while (i < messages.length) { String[] params =
		 * messages[i].split("CDATA"); for (int j = 1; j < params.length; j++) {
		 * params[j] = params[j].substring(1, params[j] .indexOf("]"));
		 * 
		 * } Log.w(TAG, "messages: " + params[1] + " " + params[2] + " " +
		 * params[3] + " " + params[4]); listOfMessages.add(new
		 * Message(params[1], params[2], params[3], params[4])); i++; }
		 * 
		 * } return listOfMessages;
		 * 
		 * } else {
		 * System.out.println("function in connection maker returns null !!");
		 * listOfMessages.add(new Message(context
		 * .getString(R.string.txt_server_down), context
		 * .getString(R.string.txt_no_message), "", "")); return listOfMessages;
		 * }
		 */
		return null;
	}

	public static boolean requestPhpSend(String pseudo, String message,
			String trainId) {
		/*
		 * ArrayList<Message> maliste = new ArrayList<Message>(); // On cree le
		 * client HttpClient client = new HttpClient();
		 * 
		 * HttpClientParams clientParams = new HttpClientParams();
		 * clientParams.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
		 * "UTF-8"); client.setParams(clientParams);
		 * 
		 * // Le HTTPMethod qui sera un Post en lui indiquant l'URL du
		 * traitement // du formulaire PostMethod methode = new PostMethod(
		 * "http://christophe.frandroid.com/betrains/php/messages.php"); // On
		 * ajoute les parametres du formulaire methode.addParameter("code",
		 * "hZkzZDzsiF5354LP42SdsuzbgNBXZa78123475621857a"); // (champs, //
		 * valeur) methode.addParameter("mode", "write");
		 * methode.addParameter("train_id", trainId);
		 * methode.addParameter("user_message", message);
		 * methode.addParameter("user_name", pseudo);
		 * 
		 * // Le buffer qui nous servira a recuperer le code de la page
		 * BufferedReader br = null; String txt = null; try { //
		 * http://hc.apache
		 * .org/httpclient-3.x/apidocs/org/apache/commons/httpclient
		 * /HttpStatus.html client.executeMethod(methode); // Pour la gestion
		 * des erreurs ou un debuggage, on recupere le // nombre renvoye. //
		 * System.out.println("La reponse de executeMethod est : " + // retour);
		 * br = new BufferedReader(new InputStreamReader(methode
		 * .getResponseBodyAsStream())); String readLine;
		 * 
		 * // Tant que la ligne en cours n'est pas vide while (((readLine =
		 * br.readLine()) != null)) { txt += readLine; } } catch (Exception e) {
		 * System.err.println(e); // erreur possible de executeMethod
		 * e.printStackTrace(); } finally { // On ferme la connexion
		 * methode.releaseConnection(); if (br != null) { try { br.close(); //
		 * on ferme le buffer } catch (Exception e) { } } }
		 * 
		 * return txt.contains("true");
		 */
		return true;
	}

	public static String getTrainId(String train) {

		String[] array = train.split("\\.");

		if (array.length == 0)
			return train;
		else
			return array[array.length - 1];

	}

	public static String formatDate(String dateFromAPI, boolean isDuration,
			boolean isDelay) {
		Date date;
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));

		if (dateFromAPI.contentEquals("0"))
			return "";
		try {
			if (isDuration) {

				if (isDelay)
					return "+" + Integer.valueOf(dateFromAPI) / 60 + "'";
				else
					date = new Date((Long.valueOf(dateFromAPI) - 3600) * 1000);
			} else {
				date = new Date((Long.valueOf(dateFromAPI)) * 1000);
			}
			return dateFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
			return dateFromAPI;
		}

	}

	public static String formatDate(long timestamp) {
		Date date = new Date(timestamp);
		DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
		return dateFormat.format(date);

	}

	public static String getHourFromDate(String dateFromAPI, boolean isDuration) {
		Date date;

		DateFormat dateFormat = new SimpleDateFormat("HH");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
		try {
			if (isDuration) {
				date = new Date((Long.valueOf(dateFromAPI) - 3600) * 1000);
			} else {

				date = new Date((Long.valueOf(dateFromAPI)) * 1000);
				Log.i("", "getHourFromDate: " + date.toString());
			}
			return dateFormat.format(date);
		} catch (Exception e) {
			return dateFromAPI;
		}

	}

	public static String getMinutsFromDate(String dateFromAPI,
			boolean isDuration) {
		Date date;
		DateFormat dateFormat = new SimpleDateFormat("mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
		try {
			if (isDuration) {
				date = new Date((Long.valueOf(dateFromAPI) - 3600) * 1000);
			} else {

				date = new Date((Long.valueOf(dateFromAPI)) * 1000);
				Log.i("", "getMinutsFromDate: " + date.toString());
			}
			return dateFormat.format(date);
		} catch (Exception e) {
			return dateFromAPI;
		}

	}

	public static String NewgetHourFromDate(long timestamp) {
		Date date;

		DateFormat dateFormat = new SimpleDateFormat("HH");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
		try {

			date = new Date(timestamp);
			Log.i("", "getHourFromDate: " + date.toString());

			return dateFormat.format(date);
		} catch (Exception e) {
			return "" + timestamp;
		}

	}

	public static String NewgetMinutsFromDate(long timestamp) {
		Date date;
		DateFormat dateFormat = new SimpleDateFormat("mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
		try {

			date = new Date(timestamp);
			Log.i("", "getMinutsFromDate: " + date.toString());
			return dateFormat.format(date);
		} catch (Exception e) {
			return "" + timestamp;
		}

	}

	public static String getDate(long i) {
		Date date = null;
		DateFormat dateFormat = new SimpleDateFormat("d MMMMM, HH:mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
		try {
			date = new Date(i);
			Log.i("", "getDate: " + date.toString());
			return dateFormat.format(date);
		} catch (Exception e) {
			return "" + i;
		}

	}

}
