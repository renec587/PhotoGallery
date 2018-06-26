package com.example.photogallery;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    private int BUTTON_SPAM = 100;
    private String captions[] = {"Video","TV","Cat","Couch","TV"};
    private String dates[] = {"2018/06/26 16:12:00","2018/05/16 23:58:27","2018/05/16 23:57:58","2018/05/16 23:57:38","2018/05/16 23:57:20"};

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityTest() {

        ViewInteraction captionView = onView(withId(R.id.textCaption));
        ViewInteraction dateView = onView(withId(R.id.textDateTime));
        ViewInteraction leftButton = onView(withId(R.id.imageButton2));
        ViewInteraction rightButton = onView(withId(R.id.imageButton));

        //Spam next button.
        int index;
        for(int i = 0; i < BUTTON_SPAM; i++) {
            if(i < captions.length) {
                index = i;
            } else {
                index = captions.length -1;
            }
            captionView.check(matches(withText(captions[index])));
            dateView.check(matches(withText(dates[index])));
            leftButton.perform(click());
        }
        index = captions.length;
        for(int i = 0; i < BUTTON_SPAM; i++) {
            index--;
            if(i >= captions.length) index = 0;
            captionView.check(matches(withText(captions[index])));
            dateView.check(matches(withText(dates[index])));
            rightButton.perform(click());
        }

        captionView.check(matches(withText("Video")));
        dateView.check(matches(withText("2018/06/26 16:12:00")));
        leftButton.perform(click());

        captionView.check(matches(withText("TV")));
        dateView.check(matches(withText("2018/05/16 23:58:27")));

        leftButton.perform(click());
        captionView.check(matches(withText("Cat")));
        dateView.check(matches(withText("2018/05/16 23:57:58")));

        leftButton.perform(click());
        captionView.check(matches(withText("Couch")));
        dateView.check(matches(withText("2018/05/16 23:57:38")));

        leftButton.perform(click());
        captionView.check(matches(withText("TV")));
        dateView.check(matches(withText("2018/05/16 23:57:20")));
    }

}
