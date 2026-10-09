package xyz.drreub.weighday.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.os.Bundle;
import android.widget.EditText;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowToast;

import xyz.drreub.weighday.R;

@RunWith(AndroidJUnit4.class)
public class SettingsFragmentTest {

    @Test
    public void newInstance_storesArguments() {
        SettingsFragment f = SettingsFragment.newInstance("a", "b");
        assertEquals("a", f.getArguments().getString("param1"));
        assertEquals("b", f.getArguments().getString("param2"));
    }

    @Test
    public void launch_withoutArguments_isFine() {
        FragmentScenario<SettingsFragment> scenario = FragmentScenario.launchInContainer(
                SettingsFragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null);
        scenario.onFragment(f -> assertNotNull(f.requireView().findViewById(R.id.some_button)));
        assertNull(f_args(scenario));
    }

    private Bundle f_args(FragmentScenario<SettingsFragment> scenario) {
        final Bundle[] b = new Bundle[1];
        scenario.onFragment(f -> b[0] = f.getArguments());
        return b[0];
    }

    @Test
    public void launch_withArguments_runsOnCreateWithArgs() {
        Bundle args = new Bundle();
        args.putString("param1", "x");
        args.putString("param2", "y");
        FragmentScenario<SettingsFragment> scenario = FragmentScenario.launchInContainer(
                SettingsFragment.class, args, R.style.Theme_Weighday, (FragmentFactory) null);
        scenario.onFragment(f -> assertEquals("x", f.getArguments().getString("param1")));
    }

    @Test
    public void button_showsEnteredTextInToast() {
        FragmentScenario<SettingsFragment> scenario = FragmentScenario.launchInContainer(
                SettingsFragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null);
        scenario.onFragment(f -> {
            ((EditText) f.requireView().findViewById(R.id.some_text)).setText("hello toast");
            f.requireView().findViewById(R.id.some_button).performClick();
        });
        assertEquals("hello toast", ShadowToast.getTextOfLatestToast());
    }
}
