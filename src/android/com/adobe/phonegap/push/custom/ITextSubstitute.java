package com.adobe.phonegap.push;

import android.content.Context;
import android.os.Bundle;

public interface ITextSubstitute {
    String parse(String text, Bundle extras, Context appContext);
}