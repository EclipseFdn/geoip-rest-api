clean:;
	rm -rf ./maxmind
	mvn clean
install: clean;
	npm ci 
	sh ./bin/maxmind.sh maxmind/
package: install;
	mvn compile package -Declipse.maxmind.root=${PWD}/maxmind
dirty-package:;
	mvn compile package -Declipse.maxmind.root=${PWD}/maxmind
headless-docker: package;
	docker-compose down
	docker-compose build
	docker-compose up -d