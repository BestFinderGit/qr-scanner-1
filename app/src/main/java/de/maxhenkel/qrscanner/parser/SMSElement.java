package de.maxhenkel.qrscanner.parser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.TextView;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.maxhenkel.qrscanner.R;
import de.maxhenkel.qrscanner.ScanResultActivity;
import de.maxhenkel.qrscanner.parser.query.Query;

public class SMSElement extends ScanElement {

    public static final Pattern SMSTO = Pattern.compile("^(?:smsto|mmsto):([^:]+)(?::([\\s\\S]*))?$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    public static final Pattern SMS = Pattern.compile("^(?:sms|mms):([^?]+)(?:\\?(.*))?$", Pattern.CASE_INSENSITIVE);

    private String[] numbers;
    private String body;
    private String action;

    public SMSElement(ScanResult result, String[] numbers, String body, String action) {
        super(result);
        this.numbers = numbers;
        this.body = body;
        this.action = action;
    }

    public static SMSElement smsTo(ScanResult result, Matcher matcher) {
        String encoded = null;
        String number = matcher.group(1);
        String[] numbers = number.split(",");
        String text = matcher.group(2);
        if (text != null) {
            try {
                encoded = URLEncoder.encode(text, "utf-8");
            } catch (Exception e) {
            }
        }

        StringBuilder sb = new StringBuilder("sms:");
        sb.append(number);
        if (encoded != null) {
            sb.append("?body=");
            sb.append(encoded);
        }

        return new SMSElement(result, numbers, text == null ? "" : text, sb.toString());
    }

    public static SMSElement sms(ScanResult result, Matcher matcher) {
        String number = matcher.group(1);
        String[] numbers = number.split(",");
        Query query = Query.parse(matcher.group(2));
        return new SMSElement(result, numbers, query.get("body").orElse(""), result.getText());
    }

    public String[] getNumbers() {
        return numbers;
    }

    public String getBody() {
        return body;
    }

    public String getAction() {
        return action;
    }

    @Override
    public Intent getIntent(Context context) {
        return new Intent(Intent.ACTION_SENDTO, Uri.parse(action));
    }

    @Override
    public int getLayout() {
        return R.layout.result_sms;
    }

    @Override
    public int getTitle() {
        return R.string.type_sms;
    }

    @Override
    public void create(ScanResultActivity activity) {
        super.create(activity);
        TextView numbers = activity.findViewById(R.id.numbers);
        numbers.setText(Arrays.stream(getNumbers()).collect(Collectors.joining(", ")));

        TextView body = activity.findViewById(R.id.body);
        body.setText(getBody());

        Button send = activity.findViewById(R.id.sendSms);
        send.setOnClickListener(v -> {
            open(activity);
        });
    }

}
