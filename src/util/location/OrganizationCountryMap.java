package util.location;

import java.io.IOException;

import rdf.RDFModel;

import log.LogManager;
import log.Logger;
import util.StringStringCSVFileCache;

public class OrganizationCountryMap extends StringStringCSVFileCache {

	private static final Logger logger = LogManager.getLogger(OrganizationCountryMap.class);

	public OrganizationCountryMap(String fileLocation) throws IOException {
		super(fileLocation);
	}

	@Override
	public String get(String organization) {
		if (hasKey(organization))
			return super.get(organization);

		logger.debug("Looking up country of organization %s", organization);
		String country = RDFModel.getCountryOfOrganization(organization);
		put(organization, country);
		return country;
	}
}
