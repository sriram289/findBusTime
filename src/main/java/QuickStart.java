import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.*;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class QuickStart {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";

    /**
     * Directory to store user credentials for this application.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/calendar-java-quickstart");

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                QuickStart.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar
    getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException, ParseException {
        EventDateTime endTimeOfLastEvent;
        Calendar service = getCalendarService();

        DateFormat justDay = new SimpleDateFormat("yyyyMMdd");
        Date thisMorningMidnight = justDay.parse(justDay.format(new Date()));
        Date tomorrowMidnight = new Date(thisMorningMidnight.getTime() + 24 * 60 * 60 * 1000);


        String calendarId = getCalendarIdOfTheTeamCalendar(service);

        List<Event> items = getListOfEventsHappeningToday(calendarId,
                service,
                new DateTime(thisMorningMidnight),
                new DateTime(tomorrowMidnight));

        System.out.println("Upcoming events");

        endTimeOfLastEvent = items.get(items.size() - 1).getEnd();

        DateFormat formatter = new SimpleDateFormat("hh:mm a");
        Date date = new Date(endTimeOfLastEvent.getDateTime().getValue());
        long time = date.getTime();
        Date dayEndsAt = new Time(new Date(time).getTime());
        Date tenMinutesFromDayEnd = new Time(new Date((10 * 60000) + time).getTime());
        Date fifteenMinutesFromDayEnd = new Time(new Date((15 * 60000) + time).getTime());

        makeTheRequest("{\"text\":\"@here :sun_with_face::end:@ " + formatter.format(dayEndsAt) + " :timer_clock::bus: \"}");
        makeTheRequest("{\"text\":\"" + formatter.format(tenMinutesFromDayEnd) + "\"}");
        makeTheRequest("{\"text\":\"" + formatter.format(fifteenMinutesFromDayEnd) + "\"}");

    }

    private static List<Event> getListOfEventsHappeningToday(String calendarId, Calendar service, DateTime minTime, DateTime maxTime) throws IOException {
        Events events = service.events().list("primary")
                .setCalendarId(calendarId)
                .setTimeMin(minTime)
                .setTimeMax(maxTime)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems();
    }

    private static String getCalendarIdOfTheTeamCalendar(Calendar service) throws IOException {
        String calendarId = null;
        String pageToken = null;
        do {
            CalendarList calendarList = service
                    .calendarList()
                    .list()
                    .setPageToken(pageToken)
                    .execute();
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                if (calendarListEntry.getSummary().contains("Dry run calendar")) {
                    calendarId = calendarListEntry.getId();
                }
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
        return calendarId;
    }

    private static void makeTheRequest(String json) {
        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpPost postRequest = new HttpPost("https://hooks.slack.com/services/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");
            postRequest.setHeader("Content-type", "application/json");
            StringEntity entity = new StringEntity(json);
            postRequest.setEntity(entity);
            HttpResponse response = httpClient.execute(postRequest);

            InputStream is = response.getEntity().getContent();
            Reader reader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();
            while (true) {
                try {
                    String line = bufferedReader.readLine();
                    if (line != null) {
                        builder.append(line);
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}