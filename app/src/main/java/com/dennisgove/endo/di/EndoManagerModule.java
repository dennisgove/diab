/*
 * Copyright 2016 Dennis Gove
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dennisgove.endo.di;


import android.content.Context;

import com.dennisgove.endo.EndoApplication;
import com.dennisgove.endo.ble.BleAdapter;
import com.dennisgove.endo.ble.BleController;
import com.dennisgove.endo.ble.BluetoothAdapterWrapper;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class EndoManagerModule {

    private EndoApplication endoApplication;

    // Constructor needs one parameter to instantiate.
    public EndoManagerModule(EndoApplication application) {
        this.endoApplication = application;
    }

    @Provides
    public EndoApplication providesEndoApplication(){
        return endoApplication;
    }

    @Provides @Named("applicationContext")
    public Context providesApplicationContext(){
        return endoApplication.getApplicationContext();
    }

    @Provides
    public BleController providesBleController(){
        return new BleController(endoApplication);
    }

    @Provides
    public BleAdapter providesBleAdapter(){
        return new BluetoothAdapterWrapper(endoApplication);
    }
}
