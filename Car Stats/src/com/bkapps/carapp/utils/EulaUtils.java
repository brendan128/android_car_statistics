/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.bkapps.carapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class EulaUtils {


  private EulaUtils() {}

  /**
   * Returns true if has accepted eula.
   * 
   * @param context the context
   */
  public static boolean hasAcceptedEula(Context context) {
    return getValue(context, false);
  }

  /**
   * Sets to true accepted eula.
   * 
   * @param context the context
   */
  public static void setAcceptedEula(Context context) {

    setValue(context, true);
  }


  
  private static boolean getValue(Context context, boolean defaultValue) {
	  SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

      return sharedPreferences.getBoolean("checkbox_preference_eula", defaultValue);
  }


  private static void setValue(Context context, boolean value) {
	  SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	  
	  SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putBoolean("checkbox_preference_eula", value);
      editor.commit();
      
  }
}
