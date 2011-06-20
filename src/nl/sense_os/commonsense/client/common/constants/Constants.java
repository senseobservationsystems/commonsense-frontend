package nl.sense_os.commonsense.client.common.constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.core.client.GWT;

public class Constants {

    /**
     * Flag for Stable mode. <code>true</code> if the app is deployed to common.sense-os.nl.
     */
    public static final boolean STABLE_MODE = GWT.getModuleBaseURL().contains("common.sense-os.nl");

    /**
     * Flag for Release Candidate mode. <code>true</code> if the app is deployed to rc.sense-os.nl.
     */
    public static final boolean RC_MODE = GWT.getModuleBaseURL().contains("rc.sense-os.nl");

    /**
     * Flag for 'ted' mode. <code>true</code> if the app is deployed to
     * commonsense-test.appspot.com.
     */
    public static final boolean TED_MODE = GWT.getModuleBaseURL().contains(
            "commonsense-test.appspot.com");

    /**
     * Flag for dev mode. <code>true</code> if the app is deployed to anything but the stable,
     * release candidate or 'ted' location.
     */
    public static final boolean DEV_MODE = !STABLE_MODE && !RC_MODE && !TED_MODE;

    /**
     * true if shortcut 'hacks' for easy developing are allowed
     */
    public static final boolean ALLOW_HACKS = !GWT.isProdMode();

    /**
     * Registry key for the list of all devices for the current user
     */
    public static final String REG_DEVICE_LIST = "DevicesList";
    /**
     * Registry key for the list of all environments for the current user
     */
    public static final String REG_ENVIRONMENT_LIST = "EnvironmentList";

    /**
     * Registry key for the list of groups for the current user
     */
    public static final String REG_GROUPS = "Groups";

    /**
     * Registry key for the list of all sensors for the current user
     */
    public static final String REG_SENSOR_LIST = "SensorsList";

    /**
     * Registry key for the list of services for the current user
     */
    public static final String REG_SERVICES = "Services";

    /**
     * Registry key for the session ID, stored as String
     */
    public static final String REG_SESSION_ID = "SessionId";

    /**
     * Registry key for the current User
     */
    public static final String REG_USER = "User";

    private Constants() {
        // Private constructor to make sure this class is not instantiated.
    }

    public static List<ModelData> getCountries() {
        List<ModelData> countries = new ArrayList<ModelData>();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("text", "AFGHANISTAN");
        properties.put("code", "AF");
        countries.add(new BaseModelData(properties));
        properties.put("text", "\u00c5LAND ISLANDS");
        properties.put("code", "AX");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ALBANIA");
        properties.put("code", "AL");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ALGERIA");
        properties.put("code", "DZ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "AMERICAN SAMOA");
        properties.put("code", "AS");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ANDORRA");
        properties.put("code", "AD");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ANGOLA");
        properties.put("code", "AO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ANGUILLA");
        properties.put("code", "AI");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ANTARCTICA");
        properties.put("code", "AQ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ANTIGUA AND BARBUDA");
        properties.put("code", "AG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ARGENTINA");
        properties.put("code", "AR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ARMENIA");
        properties.put("code", "AM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ARUBA");
        properties.put("code", "AW");
        countries.add(new BaseModelData(properties));
        properties.put("text", "AUSTRALIA");
        properties.put("code", "AU");
        countries.add(new BaseModelData(properties));
        properties.put("text", "AUSTRIA");
        properties.put("code", "AT");
        countries.add(new BaseModelData(properties));
        properties.put("text", "AZERBAIJAN");
        properties.put("code", "AZ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BAHAMAS");
        properties.put("code", "BS");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BAHRAIN");
        properties.put("code", "BH");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BANGLADESH");
        properties.put("code", "BD");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BARBADOS");
        properties.put("code", "BB");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BELARUS");
        properties.put("code", "BY");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BELGIUM");
        properties.put("code", "BE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BELIZE");
        properties.put("code", "BZ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BENIN");
        properties.put("code", "BJ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BERMUDA");
        properties.put("code", "BM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BHUTAN");
        properties.put("code", "BT");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BOLIVIA, PLURINATIONAL STATE OF");
        properties.put("code", "BO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BONAIRE, SINT EUSTATIUS AND SABA");
        properties.put("code", "BQ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BOSNIA AND HERZEGOVINA");
        properties.put("code", "BA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BOTSWANA");
        properties.put("code", "BW");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BOUVET ISLAND");
        properties.put("code", "BV");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BRAZIL");
        properties.put("code", "BR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BRITISH INDIAN OCEAN TERRITORY");
        properties.put("code", "IO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BRUNEI DARUSSALAM");
        properties.put("code", "BN");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BULGARIA");
        properties.put("code", "BG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BURKINA FASO");
        properties.put("code", "BF");
        countries.add(new BaseModelData(properties));
        properties.put("text", "BURUNDI");
        properties.put("code", "BI");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CAMBODIA");
        properties.put("code", "KH");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CAMEROON");
        properties.put("code", "CM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CANADA");
        properties.put("code", "CA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CAPE VERDE");
        properties.put("code", "CV");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CAYMAN ISLANDS");
        properties.put("code", "KY");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CENTRAL AFRICAN REPUBLIC");
        properties.put("code", "CF");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CHAD");
        properties.put("code", "TD");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CHILE");
        properties.put("code", "CL");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CHINA");
        properties.put("code", "CN");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CHRISTMAS ISLAND");
        properties.put("code", "CX");
        countries.add(new BaseModelData(properties));
        properties.put("text", "COCOS (KEELING) ISLANDS");
        properties.put("code", "CC");
        countries.add(new BaseModelData(properties));
        properties.put("text", "COLOMBIA");
        properties.put("code", "CO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "COMOROS");
        properties.put("code", "KM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CONGO");
        properties.put("code", "CG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CONGO, THE DEMOCRATIC REPUBLIC OF THE");
        properties.put("code", "CD");
        countries.add(new BaseModelData(properties));
        properties.put("text", "COOK ISLANDS");
        properties.put("code", "CK");
        countries.add(new BaseModelData(properties));
        properties.put("text", "COSTA RICA");
        properties.put("code", "CR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "C\u00d4TE D'IVOIRE");
        properties.put("code", "CI");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CROATIA");
        properties.put("code", "HR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CUBA");
        properties.put("code", "CU");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CURA�AO");
        properties.put("code", "CW");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CYPRUS");
        properties.put("code", "CY");
        countries.add(new BaseModelData(properties));
        properties.put("text", "CZECH REPUBLIC");
        properties.put("code", "CZ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "DENMARK");
        properties.put("code", "DK");
        countries.add(new BaseModelData(properties));
        properties.put("text", "DJIBOUTI");
        properties.put("code", "DJ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "DOMINICA");
        properties.put("code", "DM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "DOMINICAN REPUBLIC");
        properties.put("code", "DO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ECUADOR");
        properties.put("code", "EC");
        countries.add(new BaseModelData(properties));
        properties.put("text", "EGYPT");
        properties.put("code", "EG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "EL SALVADOR");
        properties.put("code", "SV");
        countries.add(new BaseModelData(properties));
        properties.put("text", "EQUATORIAL GUINEA");
        properties.put("code", "GQ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ERITREA");
        properties.put("code", "ER");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ESTONIA");
        properties.put("code", "EE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ETHIOPIA");
        properties.put("code", "ET");
        countries.add(new BaseModelData(properties));
        properties.put("text", "FALKLAND ISLANDS (MALVINAS)");
        properties.put("code", "FK");
        countries.add(new BaseModelData(properties));
        properties.put("text", "FAROE ISLANDS");
        properties.put("code", "FO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "FIJI");
        properties.put("code", "FJ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "FINLAND");
        properties.put("code", "FI");
        countries.add(new BaseModelData(properties));
        properties.put("text", "FRANCE");
        properties.put("code", "FR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "FRENCH GUIANA");
        properties.put("code", "GF");
        countries.add(new BaseModelData(properties));
        properties.put("text", "FRENCH POLYNESIA");
        properties.put("code", "PF");
        countries.add(new BaseModelData(properties));
        properties.put("text", "FRENCH SOUTHERN TERRITORIES");
        properties.put("code", "TF");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GABON");
        properties.put("code", "GA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GAMBIA");
        properties.put("code", "GM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GEORGIA");
        properties.put("code", "GE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GERMANY");
        properties.put("code", "DE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GHANA");
        properties.put("code", "GH");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GIBRALTAR");
        properties.put("code", "GI");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GREECE");
        properties.put("code", "GR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GREENLAND");
        properties.put("code", "GL");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GRENADA");
        properties.put("code", "GD");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GUADELOUPE");
        properties.put("code", "GP");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GUAM");
        properties.put("code", "GU");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GUATEMALA");
        properties.put("code", "GT");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GUERNSEY");
        properties.put("code", "GG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GUINEA");
        properties.put("code", "GN");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GUINEA-BISSAU");
        properties.put("code", "GW");
        countries.add(new BaseModelData(properties));
        properties.put("text", "GUYANA");
        properties.put("code", "GY");
        countries.add(new BaseModelData(properties));
        properties.put("text", "HAITI");
        properties.put("code", "HT");
        countries.add(new BaseModelData(properties));
        properties.put("text", "HEARD ISLAND AND MCDONALD ISLANDS");
        properties.put("code", "HM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "HOLY SEE (VATICAN CITY STATE)");
        properties.put("code", "VA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "HONDURAS");
        properties.put("code", "HN");
        countries.add(new BaseModelData(properties));
        properties.put("text", "HONG KONG");
        properties.put("code", "HK");
        countries.add(new BaseModelData(properties));
        properties.put("text", "HUNGARY");
        properties.put("code", "HU");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ICELAND");
        properties.put("code", "IS");
        countries.add(new BaseModelData(properties));
        properties.put("text", "INDIA");
        properties.put("code", "IN");
        countries.add(new BaseModelData(properties));
        properties.put("text", "INDONESIA");
        properties.put("code", "ID");
        countries.add(new BaseModelData(properties));
        properties.put("text", "IRAN, ISLAMIC REPUBLIC OF");
        properties.put("code", "IR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "IRAQ");
        properties.put("code", "IQ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "IRELAND");
        properties.put("code", "IE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ISLE OF MAN");
        properties.put("code", "IM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ISRAEL");
        properties.put("code", "IL");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ITALY");
        properties.put("code", "IT");
        countries.add(new BaseModelData(properties));
        properties.put("text", "JAMAICA");
        properties.put("code", "JM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "JAPAN");
        properties.put("code", "JP");
        countries.add(new BaseModelData(properties));
        properties.put("text", "JERSEY");
        properties.put("code", "JE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "JORDAN");
        properties.put("code", "JO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "KAZAKHSTAN");
        properties.put("code", "KZ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "KENYA");
        properties.put("code", "KE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "KIRIBATI");
        properties.put("code", "KI");
        countries.add(new BaseModelData(properties));
        properties.put("text", "KOREA, DEMOCRATIC PEOPLE'S REPUBLIC OF");
        properties.put("code", "KP");
        countries.add(new BaseModelData(properties));
        properties.put("text", "KOREA, REPUBLIC OF");
        properties.put("code", "KR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "KUWAIT");
        properties.put("code", "KW");
        countries.add(new BaseModelData(properties));
        properties.put("text", "KYRGYZSTAN");
        properties.put("code", "KG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "LAO PEOPLE'S DEMOCRATIC REPUBLIC");
        properties.put("code", "LA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "LATVIA");
        properties.put("code", "LV");
        countries.add(new BaseModelData(properties));
        properties.put("text", "LEBANON");
        properties.put("code", "LB");
        countries.add(new BaseModelData(properties));
        properties.put("text", "LESOTHO");
        properties.put("code", "LS");
        countries.add(new BaseModelData(properties));
        properties.put("text", "LIBERIA");
        properties.put("code", "LR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "LIBYAN ARAB JAMAHIRIYA");
        properties.put("code", "LY");
        countries.add(new BaseModelData(properties));
        properties.put("text", "LIECHTENSTEIN");
        properties.put("code", "LI");
        countries.add(new BaseModelData(properties));
        properties.put("text", "LITHUANIA");
        properties.put("code", "LT");
        countries.add(new BaseModelData(properties));
        properties.put("text", "LUXEMBOURG");
        properties.put("code", "LU");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MACAO");
        properties.put("code", "MO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF");
        properties.put("code", "MK");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MADAGASCAR");
        properties.put("code", "MG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MALAWI");
        properties.put("code", "MW");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MALAYSIA");
        properties.put("code", "MY");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MALDIVES");
        properties.put("code", "MV");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MALI");
        properties.put("code", "ML");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MALTA");
        properties.put("code", "MT");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MARSHALL ISLANDS");
        properties.put("code", "MH");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MARTINIQUE");
        properties.put("code", "MQ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MAURITANIA");
        properties.put("code", "MR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MAURITIUS");
        properties.put("code", "MU");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MAYOTTE");
        properties.put("code", "YT");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MEXICO");
        properties.put("code", "MX");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MICRONESIA, FEDERATED STATES OF");
        properties.put("code", "FM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MOLDOVA, REPUBLIC OF");
        properties.put("code", "MD");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MONACO");
        properties.put("code", "MC");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MONGOLIA");
        properties.put("code", "MN");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MONTENEGRO");
        properties.put("code", "ME");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MONTSERRAT");
        properties.put("code", "MS");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MOROCCO");
        properties.put("code", "MA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MOZAMBIQUE");
        properties.put("code", "MZ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "MYANMAR");
        properties.put("code", "MM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NAMIBIA");
        properties.put("code", "NA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NAURU");
        properties.put("code", "NR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NEPAL");
        properties.put("code", "NP");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NETHERLANDS");
        properties.put("code", "NL");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NEW CALEDONIA");
        properties.put("code", "NC");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NEW ZEALAND");
        properties.put("code", "NZ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NICARAGUA");
        properties.put("code", "NI");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NIGER");
        properties.put("code", "NE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NIGERIA");
        properties.put("code", "NG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NIUE");
        properties.put("code", "NU");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NORFOLK ISLAND");
        properties.put("code", "NF");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NORTHERN MARIANA ISLANDS");
        properties.put("code", "MP");
        countries.add(new BaseModelData(properties));
        properties.put("text", "NORWAY");
        properties.put("code", "NO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "OMAN");
        properties.put("code", "OM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "PAKISTAN");
        properties.put("code", "PK");
        countries.add(new BaseModelData(properties));
        properties.put("text", "PALAU");
        properties.put("code", "PW");
        countries.add(new BaseModelData(properties));
        properties.put("text", "PALESTINIAN TERRITORY, OCCUPIED");
        properties.put("code", "PS");
        countries.add(new BaseModelData(properties));
        properties.put("text", "PANAMA");
        properties.put("code", "PA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "PAPUA NEW GUINEA");
        properties.put("code", "PG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "PARAGUAY");
        properties.put("code", "PY");
        countries.add(new BaseModelData(properties));
        properties.put("text", "PERU");
        properties.put("code", "PE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "PHILIPPINES");
        properties.put("code", "PH");
        countries.add(new BaseModelData(properties));
        properties.put("text", "PITCAIRN");
        properties.put("code", "PN");
        countries.add(new BaseModelData(properties));
        properties.put("text", "POLAND");
        properties.put("code", "PL");
        countries.add(new BaseModelData(properties));
        properties.put("text", "PORTUGAL");
        properties.put("code", "PT");
        countries.add(new BaseModelData(properties));
        properties.put("text", "PUERTO RICO");
        properties.put("code", "PR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "QATAR");
        properties.put("code", "QA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "R\u00c9UNION");
        properties.put("code", "RE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ROMANIA");
        properties.put("code", "RO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "RUSSIAN FEDERATION");
        properties.put("code", "RU");
        countries.add(new BaseModelData(properties));
        properties.put("text", "RWANDA");
        properties.put("code", "RW");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SAINT BARTH\u00c9LEMY");
        properties.put("code", "BL");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SAINT HELENA, ASCENSION AND TRISTAN DA CUNHA");
        properties.put("code", "SH");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SAINT KITTS AND NEVIS");
        properties.put("code", "KN");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SAINT LUCIA");
        properties.put("code", "LC");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SAINT MARTIN (FRENCH PART)");
        properties.put("code", "MF");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SAINT PIERRE AND MIQUELON");
        properties.put("code", "PM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SAINT VINCENT AND THE GRENADINES");
        properties.put("code", "VC");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SAMOA");
        properties.put("code", "WS");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SAN MARINO");
        properties.put("code", "SM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SAO TOME AND PRINCIPE");
        properties.put("code", "ST");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SAUDI ARABIA");
        properties.put("code", "SA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SENEGAL");
        properties.put("code", "SN");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SERBIA");
        properties.put("code", "RS");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SEYCHELLES");
        properties.put("code", "SC");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SIERRA LEONE");
        properties.put("code", "SL");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SINGAPORE");
        properties.put("code", "SG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SINT MAARTEN (DUTCH PART)");
        properties.put("code", "SX");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SLOVAKIA");
        properties.put("code", "SK");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SLOVENIA");
        properties.put("code", "SI");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SOLOMON ISLANDS");
        properties.put("code", "SB");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SOMALIA");
        properties.put("code", "SO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SOUTH AFRICA");
        properties.put("code", "ZA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS");
        properties.put("code", "GS");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SPAIN");
        properties.put("code", "ES");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SRI LANKA");
        properties.put("code", "LK");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SUDAN");
        properties.put("code", "SD");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SURINAME");
        properties.put("code", "SR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SVALBARD AND JAN MAYEN");
        properties.put("code", "SJ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SWAZILAND");
        properties.put("code", "SZ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SWEDEN");
        properties.put("code", "SE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SWITZERLAND");
        properties.put("code", "CH");
        countries.add(new BaseModelData(properties));
        properties.put("text", "SYRIAN ARAB REPUBLIC");
        properties.put("code", "SY");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TAIWAN, PROVINCE OF CHINA");
        properties.put("code", "TW");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TAJIKISTAN");
        properties.put("code", "TJ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TANZANIA, UNITED REPUBLIC OF");
        properties.put("code", "TZ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "THAILAND");
        properties.put("code", "TH");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TIMOR-LESTE");
        properties.put("code", "TL");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TOGO");
        properties.put("code", "TG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TOKELAU");
        properties.put("code", "TK");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TONGA");
        properties.put("code", "TO");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TRINIDAD AND TOBAGO");
        properties.put("code", "TT");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TUNISIA");
        properties.put("code", "TN");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TURKEY");
        properties.put("code", "TR");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TURKMENISTAN");
        properties.put("code", "TM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TURKS AND CAICOS ISLANDS");
        properties.put("code", "TC");
        countries.add(new BaseModelData(properties));
        properties.put("text", "TUVALU");
        properties.put("code", "TV");
        countries.add(new BaseModelData(properties));
        properties.put("text", "UGANDA");
        properties.put("code", "UG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "UKRAINE");
        properties.put("code", "UA");
        countries.add(new BaseModelData(properties));
        properties.put("text", "UNITED ARAB EMIRATES");
        properties.put("code", "AE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "UNITED KINGDOM");
        properties.put("code", "GB");
        countries.add(new BaseModelData(properties));
        properties.put("text", "UNITED STATES");
        properties.put("code", "US");
        countries.add(new BaseModelData(properties));
        properties.put("text", "UNITED STATES MINOR OUTLYING ISLANDS");
        properties.put("code", "UM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "URUGUAY");
        properties.put("code", "UY");
        countries.add(new BaseModelData(properties));
        properties.put("text", "UZBEKISTAN");
        properties.put("code", "UZ");
        countries.add(new BaseModelData(properties));
        properties.put("text", "VANUATU");
        properties.put("code", "VU");
        countries.add(new BaseModelData(properties));
        properties.put("text", "VENEZUELA, BOLIVARIAN REPUBLIC OF");
        properties.put("code", "VE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "VIET NAM");
        properties.put("code", "VN");
        countries.add(new BaseModelData(properties));
        properties.put("text", "VIRGIN ISLANDS, BRITISH");
        properties.put("code", "VG");
        countries.add(new BaseModelData(properties));
        properties.put("text", "VIRGIN ISLANDS, U.S.");
        properties.put("code", "VI");
        countries.add(new BaseModelData(properties));
        properties.put("text", "WALLIS AND FUTUNA");
        properties.put("code", "WF");
        countries.add(new BaseModelData(properties));
        properties.put("text", "WESTERN SAHARA");
        properties.put("code", "EH");
        countries.add(new BaseModelData(properties));
        properties.put("text", "YEMEN");
        properties.put("code", "YE");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ZAMBIA");
        properties.put("code", "ZM");
        countries.add(new BaseModelData(properties));
        properties.put("text", "ZIMBABWE");
        properties.put("code", "ZW");
        countries.add(new BaseModelData(properties));
        return countries;
    }
}