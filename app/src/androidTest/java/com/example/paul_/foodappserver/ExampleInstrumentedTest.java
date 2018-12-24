package com.example.paul_.foodappserver;

import android.content.*;
import androidx.test.*;
import androidx.test.runner.*;

import org.junit.*;
import org.junit.runner.*;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
	@Test
	public void useAppContext() {
		// Context of the app under test.
		Context appContext = InstrumentationRegistry.getTargetContext();

		assertEquals("com.example.paul_.foodappserver", appContext.getPackageName());
	}
}
