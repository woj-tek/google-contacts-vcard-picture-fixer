package se.unir;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Photo;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.*;
import java.util.stream.Collectors;

public class PhotoFixer {

	private final static Logger log = Logger.getLogger(PhotoFixer.class.getName());

	public static void main(String[] args) {

		setupLogger();
		final PhotoFixer photoFixer = new PhotoFixer();
		photoFixer.fixContacts("/Users/wojtek/Downloads/_calendar_contacts_20199408/single/karol.vcf");
		System.exit(0);

		switch (args.length) {
			case 1:
				photoFixer.fixContacts(args[0]);
				break;
			case 2:
				photoFixer.fixContacts(args[0], args[1]);
				break;
			default:
				log.log(Level.WARNING, "Invalid number of options, please provide at least input path");
				break;
		}
	}

	private static void setupLogger() {
		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new Formatter() {
			@Override
			public String formatMessage(LogRecord record) {
				return super.formatMessage(record);
			}

			@Override
			public String format(LogRecord record) {
				return String.format("%1$s [%2$s]: %3$s\n", new Date(record.getMillis()), record.getLevel(),
									 formatMessage(record));
			}
		});
		log.setUseParentHandlers(false);
		log.addHandler(ch);
	}

	void fixContacts(String contactsPath) {
		fixContacts(contactsPath, null);
	}

	void fixContacts(String contactsPath, String outputPath) {
		final Path path = Paths.get(contactsPath);
		final File file = path.toFile();
		final List<VCard> allContacts;
		try {
			allContacts = Ezvcard.parse(file).all();
			log.log(Level.INFO, "Loaded {0} contacts, processing…", new Object[]{allContacts.size()});
			final List<VCard> collect = allContacts.stream()
					.parallel()
					.map(this::mapURLtoPhoto)
					.collect(Collectors.toList());

			final String baseName = FilenameUtils.getBaseName(contactsPath);
			final Path output = outputPath != null ? Paths.get(outputPath) : Paths.get(file.getParentFile().getPath(), baseName + "_output" + ".vcf");
			if (!output.getParent().toFile().exists()) {
				Files.createDirectories(output.getParent());
			}
			log.log(Level.INFO, "Saving {0} contacts to: {1}", new Object[]{collect.size(), output});
			Ezvcard.write(collect)
					.version(VCardVersion.V4_0)
					.go(output.toFile());
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error while processing file", e);
		}

	}

	VCard mapURLtoPhoto(VCard card) {

		final List<Photo> photos = card.getPhotos();
		log.log(Level.INFO, "Processing {0} with {1} photo URLs: {2}",
				new Object[]{card.getFormattedName().getValue(), photos.size(), photos});
		final List<Photo> collect = photos.stream()
				.map(getPhotoFromURL())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		card.removeProperties(Photo.class);
		collect.stream().forEach(card::addPhoto);
		return card;
	}

	private Function<Photo, Photo> getPhotoFromURL() {
		return photo -> {
			try {
				return new Photo(URI.create(photo.getUrl()).toURL().openStream(), photo.getContentType());
			} catch (IOException e) {
				return photo;
			}
		};
	}
}
