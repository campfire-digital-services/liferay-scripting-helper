package au.com.permeance.utility.scriptinghelper.curate;

import java.util.ArrayList;
import java.util.List;

import com.liferay.dynamic.data.mapping.annotations.DDMForm;
import com.liferay.dynamic.data.mapping.constants.DDMConstants;
import com.liferay.dynamic.data.mapping.exception.ContentException;
import com.liferay.dynamic.data.mapping.model.DDMContent;
import com.liferay.dynamic.data.mapping.render.DDMFormRenderer;
import com.liferay.dynamic.data.mapping.service.DDMContentLocalService;
import com.liferay.dynamic.data.mapping.service.persistence.DDMContentUtil;
import com.liferay.dynamic.data.mapping.storage.FieldRenderer;
import com.liferay.dynamic.data.mapping.util.DDMIndexer;
import com.liferay.dynamic.data.mapping.util.comparator.StructureIdComparator;
import com.liferay.journal.constants.JournalConstants;
import com.liferay.journal.exception.NoSuchArticleException;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleImageLocalServiceUtil;
import com.liferay.journal.service.persistence.JournalArticleFinder;
import com.liferay.journal.util.JournalContent;
import com.liferay.journal.util.comparator.ArticleIDComparator;

public class ImportPackages {

	public static void main(String[] args) throws Exception {
		
		List<Class> toImport = new ArrayList<Class>();
		
		//Journal Article import packages
		toImport.add(JournalArticleImageLocalServiceUtil.class);
		toImport.add(JournalArticleFinder.class);
		toImport.add(JournalArticle.class);
		toImport.add(JournalConstants.class);
		toImport.add(NoSuchArticleException.class);
		toImport.add(JournalContent.class);
		toImport.add(ArticleIDComparator.class);
		
		//Dynamic Data Mapping import packages
		toImport.add(DDMForm.class);
		toImport.add(DDMConstants.class);
		toImport.add(ContentException.class);
		toImport.add(DDMContent.class);
		toImport.add(DDMFormRenderer.class);
		toImport.add(DDMContentLocalService.class);
		toImport.add(DDMContentUtil.class);
		toImport.add(FieldRenderer.class);
		toImport.add(DDMIndexer.class);
		toImport.add(StructureIdComparator.class);
		
	}
	
	
}
