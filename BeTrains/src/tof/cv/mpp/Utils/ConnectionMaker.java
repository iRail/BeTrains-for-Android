package tof.cv.mpp.Utils;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tof.cv.mpp.R;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Station;
import tof.cv.mpp.bo.Train;
import tof.cv.mpp.bo.Via;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectionMaker {

	private static ConnectionDbAdapter mDbHelper;
	final static String CONNECTION = "connection";
	final static String DEPARTURE = "departure";
	final static String PLATFORM = "platform";
	final static String TIME = "time";
	final static String ARRIVAL = "arrival";
	final static String DURATION = "duration";
	final static String TIMEBETWEEN = "timeBetween";
	final static String DELAY = "delay";
	final static String TRAINS = "vehicle";
	final static String STATION = "station";
	final static String VIAS = "vias";
	final static String VIA = "via";

	public final static String[] LIST_OF_EURO_STATIONS = new String[] {
			"FR/LILLE", "FR/PARIS", "FR/STRASBOURG", "DE/AACHEN HBF",
			"DE/KOLN HBF", "LU/LUXEMBOURG", "NL/DEN HAAG", "NL/AMSTERDAM",
			"NL/MAASTRICHT", "NL/ROTTERDAM" };

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

	public static final String[] LIST_ID = new String[] {

	"205", "6", "104", "8", "70", "9", "10", "77", "12", "16", "19", "6", "22",
			"24", "25", "27", "31", "34", "35", "139", "37", "38", "30", "764",
			"58", "61", "64", "139", "37", "38", "61", "30", "764", "58", "64",
			"66", "67", "68", "70", "74", "75", "77", "78", "1841", "939",
			"82", "100", "84", "102", "102", "105", "106", "105", "106", "107",
			"110", "114", "118", "120", "124", "123", "124", "123", "126",
			"127", "128", "130", "132", "133", "136", "138", "243", "848",
			"143", "142", "146", "147", "148", "151", "151", "153", "155",
			"157", "158", "160", "166", "167", "169", "171", "1767", "177",
			"183", "184", "185", "187", "188", "189", "189", "190", "191",
			"1213", "192", "183", "708", "195", "199", "201", "203", "205",
			"208", "209", "210", "212", "210", "212", "215", "216", "217",
			"218", "219", "221", "227", "220", "215", "217", "216", "218",
			"220", "219", "221", "227", "229", "231", "232", "235", "246",
			"247", "249", "250", "252", "255", "257", "258", "259", "258",
			"259", "261", "262", "263", "266", "272", "277", "278", "992",
			"281", "286", "287", "288", "649", "289", "291", "313", "316",
			"13", "320", "317", "1761", "318", "319", "325", "324", "824",
			"326", "327", "328", "329", "327", "331", "1154", "335", "336",
			"337", "342", "345", "360", "346", "68", "347", "203", "348",
			"351", "352", "360", "361", "363", "364", "365", "366", "367",
			"368", "369", "371", "371", "375", "376", "378", "380", "382",
			"383", "1843", "384", "391", "392", "395", "399", "400", "401",
			"402", "404", "405", "406", "409", "410", "412", "413", "414",
			"415", "418", "421", "422", "423", "424", "427", "1181", "432",
			"432", "449", "455", "433", "434", "435", "436", "438", "442",
			"1670", "446", "447", "449", "455", "457", "458", "462", "470",
			"470", "471", "472", "474", "477", "479", "480", "458", "486",
			"488", "489", "493", "494", "496", "501", "504", "502", "504",
			"514", "515", "507", "510", "518", "519", "520", "1663", "521",
			"521", "523", "530", "532", "535", "539", "540", "541", "542",
			"546", "550", "553", "554", "825", "559", "560", "562", "563",
			"565", "566", "567", "568", "570", "592", "572", "574", "578",
			"579", "583", "585", "589", "590", "591", "592", "600", "601",
			"602", "604", "606", "611", "610", "610", "612", "615", "617",
			"619", "620", "621", "628", "628", "629", "630", "634", "632",
			"635", "636", "637", "642", "643", "278", "644", "647", "648",
			"649", "654", "672", "673", "673", "1744", "1744", "13", "692",
			"664", "682", "683", "684", "685", "686", "700", "699", "701",
			"704", "705", "706", "707", "708", "710", "710", "715", "719",
			"720", "723", "724", "725", "726", "728", "730", "732", "1085",
			"732", "733", "736", "738", "739", "742", "743", "744", "747",
			"748", "750", "751", "754", "759", "715", "762", "762", "726",
			"728", "730", "767", "768", "782", "781", "810", "811", "784",
			"786", "788", "789", "790", "791", "793", "797", "798", "801",
			"805", "807", "809", "810", "811", "812", "814", "815", "818",
			"819", "820", "820", "821", "822", "826", "827", "1842", "832",
			"835", "837", "868", "840", "841", "842", "848", "855", "860",
			"862", "863", "864", "877", "866", "868", "870", "871", "873",
			"894", "895", "895", "896", "897", "899", "900", "901", "752",
			"902", "905", "906", "907", "911", "908", "910", "911", "1839",
			"916", "919", "920", "923", "924", "929", "931", "933", "121",
			"929", "936", "938", "939", "941", "1666", "951", "952", "952",
			"954", "956", "957", "958", "961", "962", "968", "971", "970",
			"973", "974", "975", "976", "977", "979", "982", "984", "989",
			"991", "1013", "995", "1079", "996", "997", "1005", "1009", "1013",
			"1005", "1018", "1021", "1017", "1031", "1034", "1081", "1088",
			"1090", "1043", "1048", "1048", "1056", "1066", "1058", "1059",
			"1060", "1061", "1062", "1063", "1068", "205", "121", "1073",
			"243", "1076", "1079", "1730", "1730", "1081", "1082", "1083",
			"1084", "1087", "1088", "1090", "1091", "1092", "1093", "1097",
			"459", "1102", "1107", "1113", "1125", "1128", "1128", "1130",
			"672", "319", "1131", "1134", "1135", "1136", "1139", "1141",
			"1144", "1145", "1146", "1147", "1149", "1146", "1150", "1151",
			"1151", "1152", "1154", "1157", "335", "1159", "1160", "1160",
			"1161", "1167", "1168", "1167", "1168", "1174", "1176", "1177",
			"1177", "1180", "1180", "1181", "1182", "1184", "1185", "1186",
			"1187", "1189", "1192", "1192", "1194", "1195", "824", "1198",
			"1202", "414", "415", "1206", "1207", "1212", "1213", "1218",
			"1219", "1219", "1223", "1223", "1224", "1226", "1228", "1229",
			"1230", "1234", "1235", "1238", "1195", "1253", "1242", "1244",
			"1245", "1232", "1248", "602", "1254", "1255", "1256", "1260",
			"1261", "1262", "1723", "1265", "1266", "1270", "1272", "1092",
			"1231", "1274", "1275", "1278" };

	public static String fillZero(String mystring) {
		if (mystring.length() == 1) {
			mystring = "0" + mystring;
		}

		return mystring;

	}

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



	public static void setFullscreen(Activity context) {
		context.requestWindowFeature(Window.FEATURE_NO_TITLE);
		context.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public static void createAlertDialog(String title, String body,
			Context context) {

		AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);
		alt_bld.setMessage(body).setCancelable(false).setPositiveButton(
				android.R.string.ok, new DialogInterface.OnClickListener() {
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
		alt_bld.setMessage(body).setCancelable(false).setPositiveButton(
				android.R.string.ok, new DialogInterface.OnClickListener() {
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

	@SuppressWarnings("finally")
	public static ArrayList<Connection> newSearchTrains(String year,
			String month, String day, String hour, String minutes,
			String language, String departure, String arrival,
			String departureArrival, String trainsOnly, Context context) {
		String TAG = "BETRAINS";

		mDbHelper = new ConnectionDbAdapter(context);
		mDbHelper.open();

		// DefaultHttpClient httpclient = new DefaultHttpClient();
		String url = "http://api.irail.be/connections.php?to="
				// String url = "http://dev.api.irail.be/connections.php?to="
				+ arrival + "&from=" + departure + "&date=" + day + month
				+ year + "&time=" + hour + minutes + "&timeSel="
				+ departureArrival + "&lang=" + language + "&typeOfTransport="
				+ trainsOnly;
		url = url.replace(" ", "%20");
		Log.v(TAG, url);
		String myVersion = "0.0";
		PackageManager manager = context.getPackageManager();
		try {
			myVersion = (manager.getPackageInfo(context.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			mDbHelper.close();
		}
		/*
		 * NEW CODE FROM JAN
		 */
		ArrayList<Connection> listOfConnections = null;
		try {
			URL railTimesRequest = new URL(url);
			URLConnection yc = railTimesRequest.openConnection();
			yc.setRequestProperty("User-Agent", "BeTrains " + myVersion
					+ " for Android - " + System.getProperty("http.agent"));
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(yc.getInputStream());
			// Log.i(TAG,url);
			listOfConnections = parseConnections(document);
			mDbHelper.deleteAllConnections();
			mDbHelper.deleteAllVias();

			for (Connection conn : listOfConnections) {
				ArrayList<String> listStation = new ArrayList<String>();
				for (Via aVia : conn.getVias()) {
					listStation.add(aVia.getVehicle());
				}
				listStation.add(conn.getArrivalStation().getVehicle());

				/*
				 * public long createConnectionWithVias(String departureStation,
				 * String arrivalStation, String departureTime, String
				 * arrivalTime, String tripTime, boolean delay, String
				 * departureDate, String arrivalDate, ArrayList<String>
				 * trains,ArrayList<Via> Vias) {
				 */
				/*
				 * THIS WAS CHANGED FROM CREATECONNECTION TO
				 * CREATECONNECTIONWITHVIAS
				 */

				//mDbHelper.createConnectionWithVias(conn.getDepartureStation()
					//	.getStation(), conn.getArrivalStation().getStation(),
						//conn.getDepartureStation().getTime(), conn
							//	.getArrivalStation().getTime(), conn
								//.getDuration(), conn.getDDelay(), conn
								//.getADelay(), conn.getDepartureStation()
								//.getPlatform(), conn.getArrivalStation()
								//.getPlatform(), listStation, conn.getVias());

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			mDbHelper.close();
			return listOfConnections;
		}
	}

	public static ArrayList<Connection> parseConnections(Document document) {

		ArrayList<Connection> listConnections = new ArrayList<Connection>();
		NodeList root = document.getElementsByTagName("connections");
		System.out.println("connections : " + root.getLength());

		Node rootconnections = root.item(0);
		NodeList multipleconnection = rootconnections.getChildNodes(); // level
		// of
		// departure,
		// arrival ,
		// etc.

		// System.out.println("connection children # = "+
		// multipleconnection.getLength());
		ArrayList<String> listTrains = new ArrayList<String>();

		for (int j = 0; j < multipleconnection.getLength(); j++) {
			// System.out.println("connection children " + j + " / "+
			// multipleconnection.getLength());

			String vehicle = "";
			String platform = "";
			String time = "";
			String station = "";
			String duration = "";
			String arrivalPlatform = "";
			String departurePlatform = "";
			String arrivalTime = "";
			String departureTime = "";
			String durationAll = "";

			// TODO
			String stationCoordinates = "00000000 00000000";
			boolean platformNormal = true;
			// String delay = "x";
			String delayD = "x";
			String delayA = "x";

			Station departureStation = null;
			Station arrivalStation = null;
			ArrayList<Via> vias = new ArrayList<Via>();

			Node connection = multipleconnection.item(j);

			if (connection.getNodeName().equals(ConnectionMaker.CONNECTION)) {
				NodeList properties = connection.getChildNodes();
				for (int l = 0; l < properties.getLength(); l++) {

					Node property = properties.item(l);

					/*
					 * extract departure information of the xml stream
					 */
					if (property.getNodeName()
							.equals(ConnectionMaker.DEPARTURE)) {
						delayD = property.getAttributes().getNamedItem(
								ConnectionMaker.DELAY).getNodeValue();

						NodeList props = property.getChildNodes();
						for (int k = 0; k < props.getLength(); k++) {
							Node prop = props.item(k);
							if (prop.getNodeName().equals(
									ConnectionMaker.STATION)) {
								station = prop.getFirstChild().getNodeValue();
							} else if (prop.getNodeName().equals(
									ConnectionMaker.TIME)) {
								time = prop.getFirstChild().getNodeValue();
							} else if (prop.getNodeName().equals(
									ConnectionMaker.PLATFORM)) {
								if (prop.getFirstChild() != null)
									platform = prop.getFirstChild()
											.getNodeValue();
								else
									platform = "";
							} else if (prop.getNodeName().equals(
									ConnectionMaker.TRAINS)) {
								vehicle = prop.getFirstChild().getNodeValue();
								listTrains.add(vehicle);
							}
						}
						departureStation = new Station("", platform.trim(),
								platformNormal, time, station,
								stationCoordinates, delayD, "");

					} else if (property.getNodeName().equals(
							ConnectionMaker.ARRIVAL)) {
						delayA = property.getAttributes().getNamedItem(
								ConnectionMaker.DELAY).getNodeValue();
						NodeList props = property.getChildNodes();
						for (int k = 0; k < props.getLength(); k++) {
							Node prop = props.item(k);
							if (prop.getNodeName().equals(
									ConnectionMaker.STATION)) {
								station = prop.getFirstChild().getNodeValue();
							} else if (prop.getNodeName().equals(
									ConnectionMaker.TIME)) {
								time = prop.getFirstChild().getNodeValue();
							} else if (prop.getNodeName().equals(
									ConnectionMaker.PLATFORM)) {
								if (prop.getFirstChild() != null)
									platform = prop.getFirstChild()
											.getNodeValue();
								else
									platform = "";
							} else if (prop.getNodeName().equals(
									ConnectionMaker.TRAINS)) {
								vehicle = prop.getFirstChild().getNodeValue();
							}

						}
						arrivalStation = new Station(vehicle, platform.trim(),
								platformNormal, time, station,
								stationCoordinates, delayA, "");

					} else if (property.getNodeName().equals(
							ConnectionMaker.DURATION)) {
						durationAll = property.getFirstChild().getNodeValue();
					} else if (property.getNodeName().equals(
							ConnectionMaker.VIAS)) {
						NodeList props = property.getChildNodes();
						for (int k = 0; k < props.getLength(); k++) {
							Node prop = props.item(k);
							NodeList proper = prop.getChildNodes();
							for (int m = 0; m < proper.getLength(); m++) {
								Node propVia = proper.item(m);
								if (propVia.getNodeName().equals(
										ConnectionMaker.TRAINS)) {
									vehicle = propVia.getFirstChild()
											.getNodeValue();
								} else if (propVia.getNodeName().equals(
										ConnectionMaker.STATION)) {
									station = propVia.getFirstChild()
											.getNodeValue();
								} else if (propVia.getNodeName().equals(
										ConnectionMaker.TIMEBETWEEN)) {
									duration = propVia.getFirstChild()
											.getNodeValue();
								} else if (propVia.getNodeName().equals(
										ConnectionMaker.ARRIVAL)) {
									NodeList propArrival = propVia
											.getChildNodes();
									for (int u = 0; u < propArrival.getLength(); u++) {
										Node nodeArrival = propArrival.item(u);
										if (nodeArrival.getNodeName().equals(
												ConnectionMaker.PLATFORM)) {
											if (nodeArrival.getFirstChild() != null)
												arrivalPlatform = nodeArrival
														.getFirstChild()
														.getNodeValue();
											else
												arrivalPlatform = "";
										} else if (nodeArrival.getNodeName()
												.equals(ConnectionMaker.TIME)) {
											arrivalTime = nodeArrival
													.getFirstChild()
													.getNodeValue();
										}

									}
								} else if (propVia.getNodeName().equals(
										ConnectionMaker.DEPARTURE)) {
									NodeList propDeparture = propVia
											.getChildNodes();
									for (int u = 0; u < propDeparture
											.getLength(); u++) {
										Node nodeDeparture = propDeparture
												.item(u);
										if (nodeDeparture.getNodeName().equals(
												ConnectionMaker.PLATFORM)) {
											if (nodeDeparture.getFirstChild() != null)
												departurePlatform = nodeDeparture
														.getFirstChild()
														.getNodeValue();
											else
												departurePlatform = "";
										} else if (nodeDeparture.getNodeName()
												.equals(ConnectionMaker.TIME)) {
											departureTime = nodeDeparture
													.getFirstChild()
													.getNodeValue();
										}

									}
								}

							}
							vias.add(new Via(departurePlatform.trim(),
									departureTime, arrivalPlatform.trim(),
									arrivalTime, time, "00000000 00000000",
									station, vehicle, duration, "x"));

						}

					}

				}
				listConnections.add(new Connection(departureStation, vias,
						arrivalStation, durationAll, delayD, delayA));
			}
		}

		return listConnections;

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

	// TODO -> rewrite !!! -__- with HTML parser !
	public static ArrayList<Station> afficheGareL(String mon_url,
			Context context) {
		String TAG = "BETRAINS";
		mon_url += "&format=JSON&fast=true";
		// Toast.makeText(context,mon_url, 1).show();
		System.out
				.println("Affiche les infos train depuis la page: " + mon_url);

		long actualtime = new Date().getTime();

		ArrayList<Station> listOfStations = new ArrayList<Station>();
		listOfStations.clear();
/*
		try {
			// Log.i("MY INFO", "Json Parser started..");
			Gson gson = new Gson();
			Reader r = new InputStreamReader(getJSONData(mon_url, context));

			Liveboard obj = gson.fromJson(r, Liveboard.class);
			// TODO DISTANCE
			Log.i("NAME", "NAME" + obj.getName());
			listOfStations.add(new Station(obj.getStationInfo().getLat(), obj
					.getStationInfo().getLon(), true, "C", obj.getName(),
					"00000000 00000000", "D", "E"));
			for (Departure dep : obj.departures.departure) {
				listOfStations.add(new Station(dep.getVehicle().replace(
						"BE.NMBS.", ""), dep.getPlatform(), true, formatDate(
						dep.getTime(), false,false), ""+Html.fromHtml(dep.getName()),
						"00000000 00000000", formatDate(dep.getDelay(), true,true),
						""));
				Log.i(TAG, "adding: " + dep.getVehicle());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
*/
		return listOfStations;
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

	public static ArrayList<Train> getTrainLiveboard(String vid,
			Context context, boolean isDb) {
		// Toast.makeText(context,mon_url, 1).show();
		String TAG = "displayTrain";
		ArrayList<Train> maliste = new ArrayList<Train>();

		long actualtime = new Date().getTime();

		String langue = context.getString(R.string.url_lang_2);
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				"prefnl", false))
			langue = "nl";

		int pos = -1;
		String fromto = "";
		String mon_url = "http://api.irail.be/vehicle/?id=" + vid + "&lang="
				+ langue + "&format=json&fast=true";
		mDbHelper = new ConnectionDbAdapter(context);
		if (isDb) {
			mDbHelper.open();
			Cursor mArretCursor = mDbHelper.fetchAllWidgetStops();
			mArretCursor.moveToFirst();
			vid = mArretCursor.getString(mArretCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_STOP_NAME));
			pos = Integer.valueOf(mArretCursor.getString(mArretCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_STOP_TIME)));
			fromto = mArretCursor.getString(mArretCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_STOP_STATUS));

			mon_url = "http://api.irail.be/vehicle/?id=" + vid + "&lang="
					+ langue + "&format=json";

			mDbHelper.close();
		}
		Log.v(TAG, "Affiche les infos train depuis la page: " + mon_url);
		String txt = "";
		mDbHelper.open();
		if (isDb) {
			mDbHelper.deleteAllWidgetStops();
			mDbHelper.createWidgetStop(vid, "" + pos, "", fromto);
		}
/*		
		try {
			// Log.i("MY INFO", "Json Parser started..");
			Gson gson = new Gson();
			Reader r = new InputStreamReader(getJSONData(mon_url, context));

			TrainLiveboard obj = gson.fromJson(r, TrainLiveboard.class);
			// TODO DISTANCE
			Log.i("NAME", "NAME" + obj.getName());
			
			for (Stop stop : obj.getStops().getStop()) {

				if (isDb)
					mDbHelper.createWidgetStop(stop.station,  formatDate(stop.time,false,false),
							formatDate(stop.delay, true, true), " ");
				else
					maliste.add(new TrainStop(""+Html.fromHtml(stop.station), formatDate(stop.time,false,false),
							formatDate(stop.delay, true, true), " "));

				Log.i(TAG, "adding: " + stop.station+" - "+isDb);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
*/
		long oldTime = actualtime;
		actualtime = new Date().getTime();

		Log.i(TAG, "Scrapping time for train list: " + (actualtime - oldTime)
				+ "ms");



		if (isDb) {
			Toast.makeText(context, "Update OK", Toast.LENGTH_SHORT).show();

		}

		mDbHelper.close();

		oldTime = actualtime;
		actualtime = new Date().getTime();

		// Log.i(TAG, "Parsing time for train list: " + (actualtime - oldTime)+
		// "ms");

		return maliste;
	}

	public class TrainLiveboard {

		private Stops stops;
		private String vehicle;

		public String getName() {
			return vehicle;
		}

		public Stops getStops() {
			return stops;
		}

	}

	public class Stops {

		private String number;
		private ArrayList<Stop> stop;

		public ArrayList<Stop> getStop() {
			return stop;
		}
	}

	public class Stop {

		private String time;
		private String station;
		private String delay;

	}

	public static ArrayList<Message> requestPhpRead(String trainId, int start,
			int span, Context context) {
/*
		String TAG = "requestPhpRead";
		ArrayList<Message> listOfMessages = new ArrayList<Message>();
		// On cree le client
		HttpClient client = new HttpClient();

		HttpClientParams clientParams = new HttpClientParams();
		// clientParams.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
		// "UTF-8");
		client.setParams(clientParams);

		PostMethod methode = new PostMethod(
				"http://christophe.frandroid.com/betrains/php/messages.php");
		methode.addParameter("code",
				"hZkzZDzsiF5354LP42SdsuzbgNBXZa78123475621857a");
		methode.addParameter("message_count", "" + span);
		methode.addParameter("message_index", "" + start);
		methode.addParameter("mode", "read");
		methode.addParameter("order", "DESC");
		Log.d(TAG, "tid is " + trainId);
		if (trainId != null)
			methode.addParameter("train_id", trainId);

		// methode.addRequestHeader("Content-Type", "text/plain;charset=UTF-8");
		// Le buffer qui nous servira a recuperer le code de la page
		BufferedReader br = null;
		String txt = "";
		try {
			client.executeMethod(methode);
			br = new BufferedReader(new InputStreamReader(methode
					.getResponseBodyAsStream(), methode.getResponseCharSet()));
			String readLine;
			while (((readLine = br.readLine()) != null)) {
				System.out.println("readLine : " + readLine);
				txt += readLine;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			methode.releaseConnection();
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (!txt.equals("")) {
			String[] messages = txt.split("<message>");

			int i = 1;
			if (messages.length > 1) {

				while (i < messages.length) {
					String[] params = messages[i].split("CDATA");
					for (int j = 1; j < params.length; j++) {
						params[j] = params[j].substring(1, params[j]
								.indexOf("]"));

					}
					Log.w(TAG, "messages: " + params[1] + " " + params[2] + " "
							+ params[3] + " " + params[4]);
					listOfMessages.add(new Message(params[1], params[2],
							params[3], params[4]));
					i++;
				}

			}
			return listOfMessages;

		} else {
			System.out.println("function in connection maker returns null !!");
			listOfMessages.add(new Message(context
					.getString(R.string.txt_server_down), context
					.getString(R.string.txt_no_message), "", ""));
			return listOfMessages;
		}
*/
		return null;
	}

	public static boolean requestPhpSend(String pseudo, String message,
			String trainId) {
/*
		ArrayList<Message> maliste = new ArrayList<Message>();
		// On cree le client
		HttpClient client = new HttpClient();

		HttpClientParams clientParams = new HttpClientParams();
		clientParams.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
				"UTF-8");
		client.setParams(clientParams);

		// Le HTTPMethod qui sera un Post en lui indiquant l'URL du traitement
		// du formulaire
		PostMethod methode = new PostMethod(
				"http://christophe.frandroid.com/betrains/php/messages.php");
		// On ajoute les parametres du formulaire
		methode.addParameter("code",
				"hZkzZDzsiF5354LP42SdsuzbgNBXZa78123475621857a"); // (champs,
		// valeur)
		methode.addParameter("mode", "write");
		methode.addParameter("train_id", trainId);
		methode.addParameter("user_message", message);
		methode.addParameter("user_name", pseudo);

		// Le buffer qui nous servira a recuperer le code de la page
		BufferedReader br = null;
		String txt = null;
		try {
			// http://hc.apache.org/httpclient-3.x/apidocs/org/apache/commons/httpclient/HttpStatus.html
			client.executeMethod(methode);
			// Pour la gestion des erreurs ou un debuggage, on recupere le
			// nombre renvoye.
			// System.out.println("La reponse de executeMethod est : " +
			// retour);
			br = new BufferedReader(new InputStreamReader(methode
					.getResponseBodyAsStream()));
			String readLine;

			// Tant que la ligne en cours n'est pas vide
			while (((readLine = br.readLine()) != null)) {
				txt += readLine;
			}
		} catch (Exception e) {
			System.err.println(e); // erreur possible de executeMethod
			e.printStackTrace();
		} finally {
			// On ferme la connexion
			methode.releaseConnection();
			if (br != null) {
				try {
					br.close(); // on ferme le buffer
				} catch (Exception e) { 
				}
			}
		}

		return txt.contains("true");
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

	public static String formatDate(String dateFromAPI, boolean isDuration, boolean isDelay) {
		Date date;
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));

		if (dateFromAPI.contentEquals("0"))
			return "";
		try {
			if (isDuration) {
				
				if (isDelay)
					return "+"+Integer.valueOf(dateFromAPI)/60+"'";
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
