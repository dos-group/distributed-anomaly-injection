SRC_ROOT_DEST = anomalies/c_src
SRC_DIRS = disk_pollution fork_flooding mem_alloc

BIN_ROOT_DEST = anomalies/binaries
BIN_NAMES = disk_pollution fork_flooding mem_alloc stress-ng stress cpulimit

default: all

all: $(addprefix all,$(SRC_DIRS))

all%: 
	cd $(SRC_ROOT_DEST)/$(@:all%=%) && $(MAKE) all

install:$(addprefix install,$(BIN_NAMES))

install%:
	@mkdir -p $(BIN_ROOT_DEST)
	@(yes | cp -rf $(SRC_ROOT_DEST)/$(@:install%=%)/$(@:install%=%) $(BIN_ROOT_DEST)/$(@:install%=%) 2>/dev/null || :)
	@(yes | cp -rf $(SRC_ROOT_DEST)/$(@:install%=%)/$(@:install%=%) $(BIN_ROOT_DEST)/vnf_$(@:install%=%) 2>/dev/null || :)

clean: $(addprefix clean,$(SRC_DIRS))

clean%:
	cd $(SRC_ROOT_DEST)/$(@:clean%=%) && $(MAKE) clean

.SUFFIXES:
