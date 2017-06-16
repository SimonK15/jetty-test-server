package de.sk.jetty;

import java.lang.management.ManagementFactory;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;

import com.google.inject.Inject;
import com.google.inject.Provider;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.jetty.command.ServerCommand;
import io.bootique.meta.application.CommandMetadata;


public class StartCommand extends CommandWithMetadata {

	@Inject
	ServerCommand serverCommand;

	@Inject
	private Provider<Server> serverProvider;

	public StartCommand() {
		super(createMetadata());
	}

	private static CommandMetadata createMetadata() {
		return CommandMetadata.builder(StartCommand.class).description("Starts log collector").build();
	}

	@Override
	public CommandOutcome run(Cli cli) {
		Server server = serverProvider.get();

		//ServletContextHandler servletContextHandler  = (ServletContextHandler) server.getHandler();
		//servletContextHandler.setErrorHandler(new DefaultErrorHandler());
		
		//server.setHandler(servletContextHandler);

		MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		server.addEventListener(mbContainer);
		server.addBean(mbContainer);

		return serverCommand.run(cli);
	}
}
