package com.github.gabriel.obd2reader.classes;

import com.github.pires.obd.commands.protocol.ResetTroubleCodesCommand;

/**
 * Created by Gabriel on 05/12/2017.
 */

public class ModifiedResetTroubleCodesCommand extends ResetTroubleCodesCommand {
    @Override
    public String getResult() {
        return rawData;
    }
}
