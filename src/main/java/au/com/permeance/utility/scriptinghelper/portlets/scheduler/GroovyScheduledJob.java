package au.com.permeance.utility.scriptinghelper.portlets.scheduler;

import au.com.permeance.utility.scriptinghelper.portlets.ScriptingHelperPortlet;
import com.liferay.dispatch.executor.BaseDispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutorOutput;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.io.unsync.UnsyncPrintWriter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.scripting.ScriptingUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.UnsyncPrintWriterPool;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component(
        immediate = true,
        property = {
                "dispatch.task.executor.name=Groovy Scheduled Job",
                "dispatch.task.executor.type=groovy.scheduled.job"
        },
        service = DispatchTaskExecutor.class
)
public class GroovyScheduledJob
        extends BaseDispatchTaskExecutor {

    @Override
    public void doExecute(DispatchTrigger dispatchTrigger,
                          DispatchTaskExecutorOutput dispatchTaskExecutorOutput) throws IOException, PortalException {
        _log.info("Scheduled task executed...");
        String language = GetterUtil.getString(dispatchTrigger.getDispatchTaskSettingsUnicodeProperties().get("language"), ScriptingHelperPortlet.getDefaultLanguage());
        String script = GetterUtil.getString(dispatchTrigger.getDispatchTaskSettingsUnicodeProperties().get("script"), "script");

        Map<String, Object> portletObjects = new HashMap<>();
        UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
                new UnsyncByteArrayOutputStream();

        UnsyncPrintWriter unsyncPrintWriter = UnsyncPrintWriterPool.borrow(
                unsyncByteArrayOutputStream);

        portletObjects.put("out", unsyncPrintWriter);
        ScriptingUtil.eval(null, portletObjects, null, language, script);
        unsyncPrintWriter.flush();
        dispatchTaskExecutorOutput.setOutput(unsyncByteArrayOutputStream.toString());
    }

    @Override
    public String getName() {
        return "Groovy Scheduled Job";
    }

    Log _log = LogFactoryUtil.getLog(GroovyScheduledJob.class);

}
