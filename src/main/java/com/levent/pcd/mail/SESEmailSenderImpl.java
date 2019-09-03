package com.levent.pcd.mail;

import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailPreparationException;
import org.springframework.stereotype.Component;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.levent.pcd.service.AWSLogin;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * AWS HTML email sender
 * 
 * @author carlsamson
 *
 */
@Component("sesEmailSender")
public class SESEmailSenderImpl implements EmailModule {

	@Autowired
	private Configuration freemarkerMailConfiguration;

	
	@Autowired AWSLogin login;

	private final static String TEMPLATE_PATH = "templates/email";

	// The configuration set to use for this email. If you do not want to use a
	// configuration set, comment the following variable and the
	// .withConfigurationSetName(CONFIGSET); argument below.
	// static final String CONFIGSET = "ConfigSet";

	// The email body for recipients with non-HTML email clients.
	static final String TEXTBODY = "This email was sent through Amazon SES " + "using the AWS SDK for Java.";

	@Override
	public EmailResponse send(Email email) {

		AmazonSimpleEmailService client =login.getAmazonSESClient();
				/* AmazonSimpleEmailServiceClientBuilder.standard()
				// Replace US_WEST_2 with the AWS Region you're using for
				// Amazon SES.
				.withRegion(Regions.valueOf(region)).build();*/
		SendEmailRequest request;
		try {
			request = new SendEmailRequest()
					.withDestination(
							new Destination().withToAddresses(email.getTo()))
					.withMessage(new Message()
							.withBody(
									new Body().withHtml(new Content().withCharset("UTF-8").withData(prepareHtml(email)))
											.withText(new Content().withCharset("UTF-8").withData(TEXTBODY)))
							.withSubject(new Content().withCharset("UTF-8").withData(email.getSubject())))
					.withSource(email.getFromEmail());

			// Comment or remove the next line if you are not using a
			// configuration set

			// .withConfigurationSetName(CONFIGSET);
			client.sendEmail(request);
			EmailResponse rsp = new EmailResponse("Mail sent to" + email.getTo(), true);
			return rsp;

		} catch (Exception e) {
			e.printStackTrace();
			EmailResponse rsp = new EmailResponse("Mail sending to" + email.getTo() + " failed : " + e.getMessage(),
					true);
			return rsp;

		}

	}

	private String prepareHtml(Email email) throws Exception {

		freemarkerMailConfiguration.setClassForTemplateLoading(DefaultEmailSenderImpl.class, "/");
		Template htmlTemplate = freemarkerMailConfiguration.getTemplate(
				new StringBuilder(TEMPLATE_PATH).append("").append("/").append(email.getTemplateName()).toString());
		final StringWriter htmlWriter = new StringWriter();
		try {
			htmlTemplate.process(email.getTemplateTokens(), htmlWriter);
		} catch (TemplateException e) {
			throw new MailPreparationException("Can't generate HTML mail", e);
		}

		String html = htmlWriter.toString();
		return html;

	}

	@Override
	public void setEmailConfig(EmailConfig emailConfig) {
		// TODO Auto-generated method stub

	}

}
