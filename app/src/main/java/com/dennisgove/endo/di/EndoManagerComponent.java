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

import com.dennisgove.endo.ble.BleController;
import com.dennisgove.endo.ble.BleDevice;
import com.dennisgove.endo.ble.BluetoothAdapterWrapper;
import com.dennisgove.endo.service.EndoManager;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={ EndoManagerModule.class})
public interface EndoManagerComponent {
    void inject(EndoManager injectTarget);
    void inject(BleController injectTarget);
    void inject(BluetoothAdapterWrapper injectTarget);
    void inject(BleDevice injectTarget);
}
