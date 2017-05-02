SASS_SRC != find ./sass -type f -name '*.scss'

all: style.css

style.css: $(SASS_SRC)
	sassc --style compressed sass/style.scss src/main/resources/public/styles/style.css

watch_sass: style.css
	while true; do \
        make style.css; \
        inotifywait --quiet --recursive --event create --event modify --event move ./sass/; \
    done