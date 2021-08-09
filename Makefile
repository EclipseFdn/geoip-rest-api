clean:;
	rm -rf ./maxmind
	mvn clean
install: clean;
	sh ./bin/maxmind.sh maxmind/
package: install;
	mvn package
	docker build -f src/main/docker/Dockerfile.jvm --no-cache -t eclipsefdn/geoip-local:latest . 
dirty-package:;
	mvn package
	docker build -f src/main/docker/Dockerfile.jvm --no-cache -t eclipsefdn/geoip-local:latest . 
package-native: install;
	mvn package -Pnative -Dnative-image.docker-build=true
	docker build -f src/main/docker/Dockerfile.native --no-cache -t eclipsefdn/geoip-local-native:latest . 
dirty-package-native:;
	mvn package -Pnative -Dnative-image.docker-build=true
	docker build -f src/main/docker/Dockerfile.native --no-cache -t eclipsefdn/geoip-local-native:latest . 
headless-docker: package;
	docker run -i --rm -p 8080:8080 eclipsefdn/geoip-local:latest
headless-docker-native: package-native;
	docker run -i --rm -p 8080:8080 eclipsefdn/geoip-local-native:latest