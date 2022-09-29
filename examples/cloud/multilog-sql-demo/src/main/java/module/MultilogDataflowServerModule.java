package module;

import io.activej.csp.process.frames.FrameFormat;
import io.activej.csp.process.frames.LZ4FrameFormat;
import io.activej.dataflow.inject.DatasetId;
import io.activej.datastream.StreamSupplier;
import io.activej.datastream.StreamSupplierWithResult;
import io.activej.eventloop.Eventloop;
import io.activej.fs.ActiveFs;
import io.activej.fs.LocalActiveFs;
import io.activej.inject.annotation.Named;
import io.activej.inject.annotation.Provides;
import io.activej.inject.annotation.Transient;
import io.activej.inject.module.AbstractModule;
import io.activej.multilog.LogFile;
import io.activej.multilog.LogNamingScheme;
import io.activej.multilog.Multilog;
import io.activej.multilog.MultilogImpl;
import io.activej.promise.Promise;
import io.activej.serializer.BinarySerializer;
import misc.LogItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

import static io.activej.common.Checks.checkState;
import static module.MultilogDataflowSchemaModule.LOG_ITEM_TABLE_NAME;

public class MultilogDataflowServerModule extends AbstractModule {

	private MultilogDataflowServerModule() {
	}

	public static MultilogDataflowServerModule create() {
		return new MultilogDataflowServerModule();
	}

	@Override
	protected void configure() {
		install(MultilogDataflowSchemaModule.create());
	}

	@Provides
	@Transient
	@DatasetId(LOG_ITEM_TABLE_NAME)
	Promise<StreamSupplier<LogItem>> logItemDataset(Eventloop eventloop, Multilog<LogItem> logItemMultilog, @Named("partition") String partition) {
		checkState(eventloop.inEventloopThread());

		return logItemMultilog.read(partition, new LogFile("", 0), 0L, null)
				.map(StreamSupplierWithResult::getSupplier);
	}

	@Provides
	@Named("partition")
	String generateLogPartition() {
		int partitionId = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
		return "partition" + partitionId;
	}

	@Provides
	Multilog<LogItem> multilog(Eventloop eventloop, ActiveFs fs, FrameFormat frameFormat, BinarySerializer<LogItem> logItemSerializer, LogNamingScheme namingScheme) {
		return MultilogImpl.create(eventloop, fs, frameFormat, logItemSerializer, namingScheme);
	}

	@Provides
	ActiveFs fs(Eventloop eventloop, Executor executor) throws IOException {
		Path multilogPath = Files.createTempDirectory("multilog");
		return LocalActiveFs.create(eventloop, executor, multilogPath);
	}

	@Provides
	FrameFormat frameFormat() {
		return LZ4FrameFormat.create();
	}

	@Provides
	LogNamingScheme logNamingScheme() {
		return LogNamingScheme.NAME_PARTITION_REMAINDER;
	}
}
