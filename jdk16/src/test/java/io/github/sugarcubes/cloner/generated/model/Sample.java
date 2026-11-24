/*
 * Copyright 2017-2026 the original author or authors.
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
package io.github.sugarcubes.cloner.generated.model;

import java.util.ArrayList;
import java.util.List;

public class Sample extends Base {

    public boolean z = true;
    public byte b = 1;
    public char c = 'c';
    public short s = 2;
    public int i = 3;
    public long l = 4;
    public float f = 5.5f;
    public double d = 6.5;

    private final int finalInt;
    private final long finalLong;
    private final List<String> finalList = new ArrayList<>();
    private final Sample finalPartner;

    public String text = "text";
    public Object any = new ArrayList<>(List.of("x"));
    public int[] ints = {1, 2, 3};
    public Sample[] samples;
    public Sample self;
    public Sample partner;
    public Object nothing;

    public Sample(int finalInt, Sample finalPartner) {
        super(finalInt * 10, "base" + finalInt);
        this.finalInt = finalInt;
        this.finalLong = finalInt * 100L;
        this.finalPartner = finalPartner;
        finalList.add("item" + finalInt);
        self = this;
    }

    public int getFinalInt() {
        return finalInt;
    }

    public long getFinalLong() {
        return finalLong;
    }

    public List<String> getFinalList() {
        return finalList;
    }

    public Sample getFinalPartner() {
        return finalPartner;
    }

}
