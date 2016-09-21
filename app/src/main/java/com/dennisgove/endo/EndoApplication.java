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

package com.dennisgove.endo;

import android.app.Application;
import android.content.Intent;

import com.dennisgove.endo.di.DaggerEndoManagerComponent;
import com.dennisgove.endo.di.EndoManagerComponent;
import com.dennisgove.endo.di.EndoManagerModule;
import com.dennisgove.endo.service.EndoManager;


public class EndoApplication extends Application {

    EndoManagerComponent endoManagerComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = new Intent(this, EndoManager.class);
        startService(intent);

        endoManagerComponent = DaggerEndoManagerComponent.builder()
                .endoManagerModule(new EndoManagerModule(this))
                .build();
    }

    public EndoManagerComponent getEndoManagerComponent(){
        return endoManagerComponent;
    }
}
