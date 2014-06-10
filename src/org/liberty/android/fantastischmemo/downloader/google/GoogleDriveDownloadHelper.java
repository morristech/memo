/*
Copyright (C) 2012 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.downloader.google;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;

import android.content.Context;

import com.google.inject.assistedinject.Assisted;

public class GoogleDriveDownloadHelper {

    private Context mContext;

    private final String authToken;

    private AMFileUtil amFileUtil;

    @Inject
    public GoogleDriveDownloadHelper(Context context, @Assisted String authToken) {
        this.authToken = authToken;
        mContext = context;
    }

    @Inject
    public void setAmFileUtil(AMFileUtil amFileUtil) {
        this.amFileUtil = amFileUtil;
    }

    public List<Spreadsheet> getListSpreadsheets() throws Exception {
        List<Spreadsheet> spreadsheetList = SpreadsheetFactory.getSpreadsheets(authToken);
        return spreadsheetList;
    }

    public String downloadSpreadsheetToDB(Spreadsheet spreadsheet) throws Exception {
        List<Worksheet> worksheets = WorksheetFactory.getWorksheets(spreadsheet, authToken);

        // Find the cards worksheet, it should be with name "cards" if it was generated by AnyMemo
        // or the first worksheet if it was created by user
        List<Worksheet> cardsWorksheets = WorksheetFactory.findWorksheetByTitle(spreadsheet, "cards", authToken);
        Worksheet cardsWorksheet = null;
        if (cardsWorksheets.size() > 0) {
            cardsWorksheet = cardsWorksheets.get(0);
        } else {
            cardsWorksheet = worksheets.get(0);
        }
        Cells cardCells = CellsFactory.getCells(spreadsheet, cardsWorksheet, authToken);

        List<Worksheet> learningDataWorksheets = WorksheetFactory.findWorksheetByTitle(spreadsheet, "learning_data", authToken);
        Worksheet learningDataWorksheet = null;
        Cells learningDataCells = null;
        if (learningDataWorksheets.size() > 0) {
            learningDataWorksheet = learningDataWorksheets.get(0);
            learningDataCells = CellsFactory.getCells(spreadsheet, learningDataWorksheet, authToken);
        }

        CellsDBConverter converter = new CellsDBConverter(mContext);
        String title = spreadsheet.getTitle();
        if (!title.endsWith(".db")) {
            title += ".db";
        }
        String saveDBPath= AMEnv.DEFAULT_ROOT_PATH + "/" + title;
        String backupPath = amFileUtil.deleteFileWithBackup(saveDBPath);
        converter.convertCellsToDb(cardCells, learningDataCells, saveDBPath, backupPath);
        return saveDBPath;
    }

}
