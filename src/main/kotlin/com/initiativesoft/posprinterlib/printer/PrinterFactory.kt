package com.initiativesoft.posprinterlib.printer

import com.initiativesoft.posprinterlib.vendor.sam4s.Printer as PrinterSam4s

class PrinterFactory {

    companion object {
        fun createPrinterOfVendor(vendor: Vendor): Printer {
            if(vendor == Vendor.SAM4S) {
                return PrinterSam4s()
            }

            throw NotImplementedError("The vendor ${vendor.name} is not implemented")
        }
    }

}