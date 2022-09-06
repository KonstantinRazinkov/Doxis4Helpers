package com.sersolutions.doxis4helpers.sql.datatypes;

public interface IProgressOwner {
    void setMaxValue(int maxValue);
    void setCurrentValue(int currentValue);
    void increaseCurrentValue();
    void setProgress(double progress);


}
