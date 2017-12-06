package com.github.gabriel.obd2reader.classes;

import com.github.pires.obd.commands.control.TroubleCodesCommand;

/**
 * Created by Gabriel on 05/12/2017.
 */

public class ModifiedTroubleCodesObdCommand extends TroubleCodesCommand {
    @Override
    public String getResult() {
        return rawData.replace("SEARCHING...", "").replace("NODATA", "");
    }
}
